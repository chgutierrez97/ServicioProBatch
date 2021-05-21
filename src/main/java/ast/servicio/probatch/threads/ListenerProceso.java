package ast.servicio.probatch.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.message.Mensaje;
import ast.servicio.probatch.message.MensajeMatar;
import ast.servicio.probatch.message.MensajeProceso;
import ast.servicio.probatch.os.service.OsServiceFactory;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.LoggerUtils;
import ast.servicio.probatch.util.Utils;

public class ListenerProceso extends Listener {

	public static final char INTRO = (char) 13;
	public static Logger loggerProceso;
	public static Logger logger = LoggerFactory.getLogger(ServicioAgente.class);

	public final static String TYPE_ERROR = "stderr";
	public final static String TYPE_INPUT = "stdout";
	public final static String WARNING_UNIX = "stty: tcgetattr: Not a typewriter";
	public final static String WARNING_HPUX = "stty: : Not a typewriter";

	private OutputStream osSalida;
	private Process process;
	// private String sistemaOperativo = System.getProperty("os.name");
	private boolean cobolErrorArised;
	private PrintWriter out = null;
	public static boolean flagCobolError = false;
	private boolean mustSendIntro = false;

	public ListenerProceso(String type, Process process, OutputStream osSalidaSocket, ParametrosProceso parametroP) {
		super("Listener Proceso (" + parametroP.getId() + ")" + (TYPE_INPUT.equalsIgnoreCase(type) ? " Estandar" : " Error"));
		this.type = type;
		this.process = process;
		this.osSalida = osSalidaSocket;
		this.parametroP = parametroP;
		loggerProceso = LoggerUtils.getLoggerProceso(parametroP);
	}

	@Override
	public void run() {
		String typeMessage = "mensaje";
		try {
			String headerType = type.equals(TYPE_INPUT) ? "mensaje" : "error";
			BufferedReader in = type.equals(TYPE_INPUT) ? new BufferedReader(new InputStreamReader(process.getInputStream()))
					: new BufferedReader(new InputStreamReader(process.getErrorStream()));

			String line;
			while ((line = in.readLine()) != null && !MensajeProceso.terminarThreadsLocal) {
				String lineResult = validaPatron(parametroP.getPatrones(), line);

				// logger.debug("lineResult: {}", lineResult);
				if ("".equals(lineResult))
					continue;

				if ("error".equals(headerType))
					loggerProceso.error(StringEscapeUtils.escapeXml(lineResult));
				else
					loggerProceso.info(StringEscapeUtils.escapeXml(lineResult));

				lineResult = OsServiceFactory.getOsService().resuelveVariablesDeSistema(lineResult);

				Mensaje mensaje = MessageFactory.crearMensajeRespuesta(String.format("<%s id=\"%s\" nombre=\"%s\" ts=\"%d\">%s</%s>", typeMessage,
						parametroP.getId(), parametroP.getNombre(), parametroP.getTs(), StringEscapeUtils.escapeXml(lineResult), typeMessage));

				String mensajeEnviar = mensaje.getTramaString();
				// only Solaris.
				if (Utils.clearGarbageSolaris(mensajeEnviar)) {
					logger.debug("CLI <-- {}", mensajeEnviar);
					osSalida.write(mensajeEnviar.getBytes());
				}

				// solo para script cobol.
				if (cobolErrorArised) {
					sendIntroAndCloseStream();
					// process.destroy();
					flagCobolError = true;
					throw new MensajeErrorException("error", parametroP.getId(), parametroP.getNombre(), "killing cobol process");
				}

				if (mustSendIntro) {
					sendIntro();
					mustSendIntro = false; // Una vez enviado el enter,
											// desactivo la bandera
				}
			} // fin de while

			logger.debug("Listener de tipo [{}] del proceso {} termino normalmente", headerType, parametroP.getId());

		} catch (MensajeErrorException e) {
			logger.debug("APARECIO MensajeErrorException! SE MATARA EL PROCESO " + parametroP.getPid() + "; MENSAJE ERROR: " + e.getRespuestaError());
			setErrorFatal(e.getRespuestaError().toString());
			MensajeMatar.matar(parametroP.getPid());
			logger.info(e.getRespuestaError().toString());
			logger.trace(e.getMessage());
		} catch (IOException e) {
			System.err.println("Error al enviar el mensaje");
			loggerProceso.error("Error al enviar el mensaje");
			logger.error("Error al enviar el mensaje");
			logger.trace(e.getMessage());
			ServicioAgente.connectionStatus = false;
			EjecutarProceso.procesoTerminado = true;
		} catch (Exception e) {
			loggerProceso.error("Error al ejecutar el proceso");
			logger.error("Error al ejecutar el proceso ", e);
			logger.trace(e.getMessage());
		}
	}

	/**
	 * Evalua si la salida cumple con alguna expresion regular o patron de
	 * caracteres esperado.
	 *
	 * @param patrones
	 *            - Patrones a validar contra la salida.
	 * @param salidaProceso
	 *            - Salida a comparar.
	 * @return Un string (original o modificado) en caso que salidaProceso
	 *         corresponda con alguno de los patrones. null en caso contrario.
	 * @throws MensajeErrorException
	 */
	public String validaPatron(Collection<Atributo> patrones, String salidaProceso) throws MensajeErrorException {
		String[] ignore_re = ServicioAgente.cfg.getIgnore_re().split(",");

		if (ignore_re.length > 0)
			salidaProceso = validaExpresionByMantis(salidaProceso, ignore_re);

		// Si no hay patrones que validar, termino tempranamente
		if (patrones.isEmpty())
			return salidaProceso;

		for (Atributo atributo : patrones) {
			boolean isFatal = ParametrosProceso.FATAL.equals(atributo.getNombre());
			String tipo = atributo.getTipo();

			if (isFatal && "literal".equals(tipo) && Utils.validaLiterales(salidaProceso, atributo.getValor())) {
				throw new MensajeErrorException("fatal", parametroP.getId(), parametroP.getNombre(), salidaProceso);
			}

			if (isFatal && "re".equals(tipo) && Utils.validaExpresionesRegulares(salidaProceso, atributo.getValor())) {
				throw new MensajeErrorException("fatal", parametroP.getId(), parametroP.getNombre(), salidaProceso);
			}
		}

		for (Atributo atributo : patrones) {
			String tipo = atributo.getTipo();

			boolean isIgnore = ParametrosProceso.IGNORAR.equals(atributo.getNombre());
			if (isIgnore && "glob".equals(tipo) && Utils.validaExpresionesRegulares(salidaProceso, atributo.getValor()))
				return "";
			if (isIgnore && "re".equals(tipo) && Utils.validaExpresionesRegulares(salidaProceso, atributo.getValor())) {
				salidaProceso = salidaProceso.replaceAll(atributo.getValor(), "");
				return salidaProceso.trim();
			}
			if (isIgnore && "literal".equals(tipo) && Utils.validaLiterales(salidaProceso, atributo.getValor()))
				return "";

			boolean isCapture = ParametrosProceso.CAPTURAR.equals(atributo.getNombre());
			if (isCapture && "literal".equals(tipo))
				cobolErrorArised = Utils.validarMensajeCobolLiteral(salidaProceso, atributo.getValor());
			if (isCapture && "re".equals(tipo))
				cobolErrorArised = Utils.validaExpresionesRegulares(salidaProceso, atributo.getValor());

			boolean isIntro = ParametrosProceso.INTRO.equals(atributo.getNombre());
			if (isIntro && "literal".equals(tipo))
				mustSendIntro = Utils.validarMensajeCobolLiteral(salidaProceso, atributo.getValor());
			if (isIntro && "re".equals(tipo))
				mustSendIntro = Utils.validaExpresionesRegulares(salidaProceso, atributo.getValor());
		}

		return salidaProceso;
	}

	public String validaExpresionByMantis(String cadena, String[] ignore_regexps) {

		/*
		 * se verifica si existe un tipo de mensajes (a partir de una expresion
		 * regular) que se deben ignorar. Si la expresion regular en
		 * ignore_regexps aplica a salidaProceso -> se debe parsear el mensaje
		 * para que ignore lo establecido en el parametro ignore_regexps del
		 * archivo de configuracion "mantis"
		 */
		String regex;
		String cadena_modificada;
		for (String ignore_re : ignore_regexps) {
			if (Utils.validaExpresionesRegulares(cadena, ignore_re.trim())) {
				cadena_modificada = cadena.replaceAll(ignore_re.trim(), "");
				return cadena_modificada;
			}
		}
		return cadena;
	}

	/**
	 * este metodo es especifico para el sistema operatico Solaris donde existen
	 * unos script de Cobol, que al finalizar con error muestran el mensaje
	 * "Presione una tecla para continuar.." esperando que el usuario prsione
	 * Enter. ya que probatch no es interactivo se opto por mandarle un char 13
	 * que equivale a la misma tecla cuando se origine dicho mensaje, de esta
	 * manera el proceso no queda en stand by.
	 */
	private void sendIntroAndCloseStream() {
		logger.debug("ENVIANDO ENTER PARA PROCESO {}", parametroP.getId());
		out = new PrintWriter(process.getOutputStream());
		out.print(INTRO);
		out.flush();
		out.close();
	}

	/**
	 * Envia un enter al STDIN del proceso.
	 */
	private void sendIntro() {
		logger.debug("ENVIANDO ENTER PARA PROCESO {}", parametroP.getId());
		PrintWriter writer = new PrintWriter(process.getOutputStream(), true);
		writer.println();
	}

}
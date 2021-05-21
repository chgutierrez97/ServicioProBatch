package ast.servicio.probatch.threads;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.JobLog;
import com.ibm.as400.access.QueuedMessage;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.message.Mensaje;
import ast.servicio.probatch.os.service.impl.as400.AS400Process;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.LoggerUtils;
import ast.servicio.probatch.util.Utils;

public class ListenerProcesoAS400 extends Listener {
	public static Logger loggerProceso;
	public static Logger logger = LoggerFactory.getLogger(ServicioAgente.class);
	private AS400Process process;
	private ParametrosProceso parametroP;
	private OutputStream osSalida;
	private String errorFatal;

	public ListenerProcesoAS400(Process process, OutputStream osSalidaSocket, ParametrosProceso parametroP) {
		super("Listener Proceso AS400 (" + parametroP.getId() + ")" + " Estandar");
		this.process = (AS400Process) process;
		this.parametroP = parametroP;
		this.osSalida = osSalidaSocket;
		this.type = "as400";
		loggerProceso = LoggerUtils.getLoggerProceso(parametroP);
	}

	public String getErrorFatal() {
		return this.errorFatal;
	}

	public void setErrorFatal(String errorFatal) {
		this.errorFatal = errorFatal;
	}

	@Override
	public void run() {

		String typeMessage = "mensaje";
		String lineResult = null;
		CommandCall cmd = this.process.getCmd();
		AS400 conexion = cmd.getSystem();
		JobLog logTrabajoMon = null;
		byte[] last = null;
		try {
			conexion.connectService(2);
			AS400 conexionAux = new AS400(conexion);
			conexionAux.connectService(2);

			// Job job = cmd.getServerJob();

			
			
			logger.debug("INTERNAL JOB IDENTIFIER: " + process.getJobId());
			Job jobMon = new Job(conexionAux, process.getJobId());

			logTrabajoMon = jobMon.getJobLog();
			logTrabajoMon.setStartingMessageKey(last);

			Enumeration listaMensaje = logTrabajoMon.getMessages();

			while (listaMensaje.hasMoreElements()) {
				QueuedMessage message = (QueuedMessage) listaMensaje.nextElement();
				lineResult = message.getFromProgram() + ":" + message.getAlertOption() + ":" + message.getSeverity() + ":" + message.getText();
				if (validaPatron(this.parametroP.getPatrones(), lineResult) == null) {
					Mensaje mensaje = MessageFactory.crearMensajeRespuesta(String.format("<%s id=\"%s\" nombre=\"%s\" ts=\"%d\">%s</%s>", typeMessage,
							parametroP.getId(), parametroP.getNombre(), parametroP.getTs(), StringEscapeUtils.escapeXml(lineResult), typeMessage));
					loggerProceso.info(StringEscapeUtils.escapeXml(lineResult));
					logger.debug("CLI <-- " + mensaje.getTramaString());
					this.osSalida.write(mensaje.getTramaString().getBytes());
					last = message.getKey();
				}

			}

		} catch (MensajeErrorException e) {
			logger.error("Error al enviar el mensaje",e);
			setErrorFatal(e.getRespuestaError().toString());
			logger.info(e.getRespuestaError().toString());
			logger.trace(e.getMessage());
		} catch (IOException e) {
			logger.error("Error al enviar el mensaje",e);
			System.err.println("Error al enviar el mensaje");
			loggerProceso.error("Error al enviar el mensaje");
			logger.error("Error al enviar el mensaje");
			logger.trace(e.getMessage());
			ServicioAgente.connectionStatus = false;
			EjecutarProceso.procesoTerminado = true;
		} catch (Exception e) {
			logger.error("Error al enviar el mensaje",e);
			loggerProceso.error("Error al ejecutar el proceso");
			logger.error("Error al ejecutar el proceso: " + e.getMessage());
			logger.trace(e.getMessage());
			ServicioAgente.connectionStatus = false;
		}
	}

	public String validaPatron(Collection<Atributo> patrones, String salidaProceso) throws MensajeErrorException {
		if (!patrones.isEmpty()) {
			for (Atributo atributo : patrones) {
				if (("fatal".equals(atributo.getNombre())) && (Utils.validaExpresionesRegulares(salidaProceso, atributo.getValor()))) {
					throw new MensajeErrorException("fatal", this.parametroP.getId(), this.parametroP.getNombre(), salidaProceso);
				}
			}

			for (Atributo atributo : patrones) {
				if (("ignorar".equals(atributo.getNombre())) && (atributo.getTipo().equals("glob"))
						&& (Utils.validaExpresionesRegulares(salidaProceso, atributo.getValor()))) { return salidaProceso; }
				if (("ignorar".equals(atributo.getNombre())) && (atributo.getTipo().equals("re"))
						&& ((Utils.validaExpresionesRegulares(salidaProceso, atributo.getValor()))
						|| (Utils.validaExpresionesRegulares(salidaProceso, ServicioAgente.cfg.getIgnore_re())))) {
					return salidaProceso;
				}
			}
		}

		return null;
	}

}
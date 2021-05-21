package ast.servicio.probatch.threads;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.connection.OutputWriter;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.message.Mensaje;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;

public class ServiceProcessMessage extends Thread {

	public static Logger logger = LoggerFactory.getLogger(EjecutarProceso.class);
	public static Logger loggerProceso;
	InputStream entrada = null;
	OutputWriter output = null;
	String strMensaje = null;

	public ServiceProcessMessage(InputStream entrada, OutputWriter output, String strMensaje) throws IOException {
		this.entrada = entrada;
		this.output = output;
		this.strMensaje = strMensaje;

	}

	@Override
	public void run() {

		logger.info("procesando " + strMensaje + "...");
		List<String> xmlList = Utils.obtenerCadenas(strMensaje, "" + (char) 13 + (char) 10);

		try {

			for (String xmlString : xmlList) {
				Mensaje respuesta = procesarRequerimiento(xmlString, output);

				if (respuesta != null) {

					if (respuesta.getTramaString().length() > ServicioAgente.cfg.getOutput_maxsize()) {
						String msjRecortado = respuesta.getTramaString().substring(0, ServicioAgente.cfg.getOutput_maxsize()) + "\n";
						output.write(msjRecortado.getBytes());
					} else {
						// output.write(respuesta.getTramaString().concat("\n").getBytes());
						output.write(respuesta.getTramaString().getBytes());
					}

					output.flush();
				}
			}

		} catch (IOException e) {
			// Logs
			logger.error("El cliente cerro la conexion");
			logger.error(e.getMessage());
			ServicioAgente.connectionStatus = false;
		} catch (Exception e) {
			// Logs
			logger.error("Exception: " + e + "Message: " + e.getMessage() + "Cause: " + e.getCause());
			logger.trace("Exception: " + e + "Message: " + e.getMessage() + "Cause: " + e.getCause());
			ServicioAgente.connectionStatus = false;
		}

	}

	private Mensaje procesarRequerimiento(String strMensaje, OutputStream osSalida) {
		Mensaje respuesta = null;
		try {
			Mensaje mensajeEntrada = MessageFactory.crearMensaje(strMensaje);
			respuesta = mensajeEntrada.procesarMensaje(osSalida);
			if (respuesta != null) {
				logger.debug("CLI <-- " + respuesta.getTramaString());
			}
		} catch (MensajeErrorException e) {
			// Logs
			respuesta = MessageFactory.crearMensajeRespuesta(e.getRespuestaError().toString());
			logger.error(e.getRespuestaError().toString());
			logger.trace(e.getMessage());
		}
		return respuesta;
	}

}

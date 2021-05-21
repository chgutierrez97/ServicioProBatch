package ast.servicio.probatch.message;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;

/**
 * Devuelve información sobre los procesos lanzados por el servidor. Se puede
 * obtener estados desde un estado determinado a otro (desde="n1" hasta="n3").
 * 
 * @author rodrigo.guillet
 * 
 */
public class MensajeEstado extends Mensaje {

	public MensajeEstado(String mensajeEntrada) {
		super(mensajeEntrada);
	}

	public Mensaje procesarMensaje(OutputStream osSalida) throws MensajeErrorException {
		Logger logger = LoggerFactory.getLogger(getClass());

		logger.debug("MensajeEstado solicita lista estado mensajes...");
		List<EstadoProceso> listaEstados = ServicioAgente.getEstadoMensajes();
		StringBuffer listaTransicionEstados = new StringBuffer();
		String etiqueta = Utils.obtenerParametroTramaString(this.getTramaString(), "etiqueta");
		if (!listaEstados.isEmpty()) {
			String desdeString = Utils.obtenerParametroTramaString(this.getTramaString(), "desde");
			String hastaString = Utils.obtenerParametroTramaString(this.getTramaString(), "hasta");
			if (desdeString.equals("") || hastaString.equals(""))
				throw new MensajeErrorException("Error en la sintaxis del mensaje. atributo 'desde' y/o 'hasta' son vacios");

			int desde = new Integer(desdeString);
			int hasta = new Integer(hastaString);
			synchronized (listaEstados) {
				for (Iterator<EstadoProceso> iterator = listaEstados.iterator(); iterator.hasNext();) {
					EstadoProceso estadoProceso = iterator.next();
					Integer id = new Integer(estadoProceso.getId());
					String s_estadoProceso = estadoProceso.getMensajeTransicionEstado().getTramaString();
					if (id >= desde && id <= hasta) {
						listaTransicionEstados.append(s_estadoProceso);
					}

				}
			}

		} else {
			logger.debug("listaEstados ESTA VACIA!");
		}

		String s_listaTransicionEstados = listaTransicionEstados.toString();

		Mensaje returnMessage = MessageFactory.crearMensajeRespuesta("estado", etiqueta, null, null, s_listaTransicionEstados, null, false);

		String s_returnMessage = returnMessage.getTramaString();
		s_returnMessage = s_returnMessage.replace("\n", "").replace("\r", "");
		returnMessage.setTramaString(s_returnMessage);
		logger.debug("returnMessage.getTramaString()=" + returnMessage.getTramaString());

		logger.debug("s_returnMessage=" + s_returnMessage);
		if (s_returnMessage.contains("\n")) {
			logger.debug("returnMessage TIENE SALTO DE LINEA!");
		}

		return returnMessage;
	}

}

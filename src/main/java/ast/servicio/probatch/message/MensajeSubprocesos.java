package ast.servicio.probatch.message;

import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.monitoring.MonitorProcess;
import ast.servicio.probatch.monitoring.MonitorProcessFactory;
import ast.servicio.probatch.os.service.OsServiceFactory;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;

public class MensajeSubprocesos extends Mensaje {

	Logger logger = LoggerFactory.getLogger(MensajeSubprocesos.class);

	public MensajeSubprocesos(String mensajeEntrada) {
		super(mensajeEntrada);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Mensaje procesarMensaje(OutputStream osSalida) throws MensajeErrorException {

		String proceso_pid = Utils.obtenerParametroTramaString(this.getTramaString(), "id");
		String os_name = OsServiceFactory.checkOs();
		StringBuilder respuestaString = new StringBuilder();

		List<EstadoProceso> listaEstados = ServicioAgente.getEstadoMensajes();
		for (EstadoProceso estadoProceso : listaEstados) {
			if (estadoProceso.getId().equals(proceso_pid)) {
				try {
					MonitorProcess monitor = MonitorProcessFactory.getMonitorProcess(os_name);

					List<String> listaEstadosSubprocesos = monitor.getSubprocessesState(Integer.valueOf(estadoProceso.getPid()));
					respuestaString.append("<consulta-procesos>");
					for (String status : listaEstadosSubprocesos) {
						respuestaString.append(status);
					}
					respuestaString.append("</consulta-procesos>");

				} catch (Exception ex) {
					logger.debug("Error : " + ex.getMessage());
				}
			}
		}

		this.setTramaString(respuestaString.toString());
		return this;
	}

}

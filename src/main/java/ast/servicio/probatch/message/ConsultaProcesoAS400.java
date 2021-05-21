package ast.servicio.probatch.message;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.JobList;

import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.service.ServicioAgente;

public class ConsultaProcesoAS400 extends Mensaje {

	public static Logger logger = LoggerFactory.getLogger(ConsultaProcesoAS400.class);

	public ConsultaProcesoAS400(String mensajeEntrada) {
		super(mensajeEntrada);
	}

	@Override
	public Mensaje procesarMensaje(final OutputStream osSalida) throws MensajeErrorException {
		logger.debug("consultando procesos de as400...");
		String nombreSubProc = "";
		String nombreUsuarioEje = "";
		final StringBuffer mensajeString = new StringBuffer("<procesos-as400>");
		try {
			nombreSubProc = this.getTramaXml().getDocumentElement().getAttribute("nombre");
			nombreUsuarioEje = this.getTramaXml().getDocumentElement().getAttribute("usuario");
			final AS400 sys = new AS400(ServicioAgente.cfg.getaS400Server());
			
			String user = ServicioAgente.cfg.getaS400User();
			sys.setUserId(user);
			
			String pass = ServicioAgente.cfg.getaS400Pass();
			sys.setPassword(pass);
			
			final JobList jobList = new JobList(sys);
			if (!nombreSubProc.equals("")) {
				jobList.addJobSelectionCriteria(1, (Object) nombreSubProc);
			} else {
				if (nombreUsuarioEje.equals("")) {
					mensajeString.append("Se debe introducir el nombre del proceso o el usuario que de ejecucion");
					mensajeString.append("</procesos-as400>");
					return MessageFactory.crearMensajeRespuesta(mensajeString.toString());
				}
				jobList.addJobSelectionCriteria(2, (Object) nombreUsuarioEje);
			}
			Enumeration list = jobList.getJobs();
			while (list.hasMoreElements()) {
				mensajeString.append("<proceso");
				final Job j = (Job) list.nextElement();
				try {
					String status = j.getStatus();
					if (status.equals("*ACTIVE")) {
						status = String.valueOf(status) + "/" + j.getValue(101);
					} else if (status.equals("*OUTQ")) {
						String estadoFinal = new String();
						estadoFinal = j.getCompletionStatus();
						if (estadoFinal.equals(" ")) {
							status = String.valueOf(status) + "/" + "NOT_COMPLETED";
						} else if (estadoFinal.equals("1")) {
							status = String.valueOf(status) + "/" + "COMPLETED_ABNORMALLY";
						} else if (estadoFinal.equals("0")) {
							status = String.valueOf(status) + "/" + "COMPLETED_NORMALLY";
						}
					}
					final String j_name = j.getName().trim();
					final String j_user = j.getUser().trim();
					final String j_number = j.getNumber().trim();
					final String j_status = status.trim();
					mensajeString.append(" nombre=\"" + j_name + "\"");
					mensajeString.append(" usuario=\"" + j_user + "\"");
					mensajeString.append(" numero=\"" + j_number + "\"");
					mensajeString.append(" estado=\"" + j_status + "\"");
					final Date jobActiveDate = j.getJobActiveDate();
					final String s_horaInicio = formatISO8601Date(jobActiveDate).trim();
					mensajeString.append(" hora-inicio=\"" + s_horaInicio + "\" />");
				} catch (Exception e) {
					logger.debug(e.getMessage());
					throw new MensajeErrorException(e.getMessage());
				}
			}
			mensajeString.append("</procesos-as400>");
			
		} catch (Exception e2) {
			logger.debug(e2.getMessage());
			logger.info(new MensajeError("Error al procesar la lista de trabajos").getTramaString());
			throw new MensajeErrorException("Error al procesar la lista de trabajos" + e2.getMessage());
		}
		return MessageFactory.crearMensajeRespuesta(mensajeString.toString());
	}

	private String formatISO8601Date(final Date date) {
		String fecha = format(date, "yyyy-MM-dd HH:mm:ss");
		fecha = fecha.replace(" ", "T");
		return fecha;
	}

	private String format(final Date date, final String format) {
		String result = "";
		if (date != null) {
			try {
				final SimpleDateFormat shortDateFormat = new SimpleDateFormat(format);
				result = shortDateFormat.format(date);
			} catch (Exception ex) {
			}
		}
		return result;
	}

}

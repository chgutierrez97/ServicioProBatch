package ast.servicio.probatch.message;

import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.SystemValue;

import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.service.ServicioAgente;

public class MensajeHoraAS400 extends Mensaje {

	public static Logger logger = LoggerFactory.getLogger(MensajeHoraAS400.class);

	public MensajeHoraAS400(String mensajeEntrada) {
		super(mensajeEntrada);
	}

	public Mensaje procesarMensaje(OutputStream osSalida) throws MensajeErrorException {
		logger.debug("Solicita hora de AS400");
		final StringBuffer msjHoraAS = new StringBuffer();
		msjHoraAS.append("<hora-as400 hora= \"");
		msjHoraAS.append(this.horaSistema());
		msjHoraAS.append("\">");
		this.setTramaString(msjHoraAS.toString());
		return this;
	}

	private String horaSistema() throws MensajeErrorException {
		final StringBuffer fechaHora = new StringBuffer();
		try {
			final AS400 sys = new AS400();
			sys.setGuiAvailable(false);
			sys.setSystemName(ServicioAgente.cfg.getaS400Server());
			
			String user = ServicioAgente.cfg.getaS400User();
			sys.setUserId(user);
			
			
			String pass = ServicioAgente.cfg.getaS400Pass();
			sys.setPassword(pass);
			
			
			final SystemValue sysASFecha = new SystemValue(sys, "QDATE");
			final SystemValue sysASHora = new SystemValue(sys, "QTIME");
			fechaHora.append(String.valueOf(sysASFecha.getValue().toString()) + "T" + sysASHora.getValue().toString());
		} catch (Exception e) {
			throw new MensajeErrorException("No se pudo obtener la hora del as400 " + e.getMessage());
		}
		return fechaHora.toString();
	}

}

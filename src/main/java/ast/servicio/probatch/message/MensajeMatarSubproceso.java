package ast.servicio.probatch.message;

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.os.service.OsServiceFactory;
import ast.servicio.probatch.util.Utils;

public class MensajeMatarSubproceso extends Mensaje{
	
	Logger logger = LoggerFactory.getLogger(MensajeSubprocesos.class);

	public MensajeMatarSubproceso(String mensajeEntrada) {
		super(mensajeEntrada);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Mensaje procesarMensaje(OutputStream osSalida) throws MensajeErrorException {

		String pid = Utils.obtenerParametroTramaString(this.getTramaString(), "pid");
				
		String killCommand = OsServiceFactory.getOsService().getKillCommand(Integer.valueOf(pid));
		try {
			Runtime.getRuntime().exec(killCommand);
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		return this;
	}
	
	
	
}

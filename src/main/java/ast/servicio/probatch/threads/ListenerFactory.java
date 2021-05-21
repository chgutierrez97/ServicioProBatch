package ast.servicio.probatch.threads;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.os.service.OsServiceFactory;

public class ListenerFactory {

	private static final Logger logger = LoggerFactory.getLogger(ListenerFactory.class);
	
	private ListenerFactory() {

	}

	public static List<? extends Listener> getListeners(Process process, OutputStream osSalida, ParametrosProceso parametroP) {

		if (OsServiceFactory.isAS400SO()) {
			logger.debug("Creando listener para AS400");
			ListenerProcesoAS400 procesoInput = new ListenerProcesoAS400(process, osSalida, parametroP);
			procesoInput.setDaemon(true);
			procesoInput.start();
			logger.debug("Listener para AS400 iniciado");
			return Collections.singletonList(procesoInput);
		} else {
			logger.debug("Creando listener para INPUT");
			ListenerProceso procesoInput = new ListenerProceso(ListenerProceso.TYPE_INPUT, process, osSalida, parametroP);
			procesoInput.setDaemon(true);
			logger.debug("Listener para INPUT iniciado");
			procesoInput.start();

			logger.debug("Creando listener para ERROR");
			ListenerProceso procesoError = new ListenerProceso(ListenerProceso.TYPE_ERROR, process, osSalida, parametroP);
			procesoError.setDaemon(true);
			logger.debug("Listener para ERROR iniciado");
			procesoError.start();

			return Arrays.asList(procesoInput, procesoError);
		}

	}

}

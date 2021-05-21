package ast.servicio.probatch.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MonitorProcessFactory {

	static Logger logger = LoggerFactory.getLogger(MonitorProcessFactory.class);
	
	public static MonitorProcess getMonitorProcess(String key) throws Exception {

		MonitorProcess monitor = null;
		

		if (key.contains("linux")) {
			monitor = new MonitorProcessLinux();
		} else if (key.contains("aix")) {
			monitor = new MonitorProcessUnix();
		} else if (key.contains("windows")) {
			monitor = new MonitorProcessWindows();
			logger.debug("crea instancia de windows...");
		} else if (key.contains("sunos")) {
			monitor = new MonitorProcessSolaris();
		} else {
			throw new Exception("No se reconoce instancia del sistema operativo");
		}
		
				
		
		return monitor;

	}

}

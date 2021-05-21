package ast.servicio.probatch.os.service;

import ast.servicio.probatch.configuration.Configurador;
import ast.servicio.probatch.os.service.impl.*;
import ast.servicio.probatch.service.ServicioAgente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsServiceFactory {
    private static Configurador cfg;
    private static final String UNIX = "aix";
    private static final String HPUX = "hp-ux";
    private static final String lINUX = "nux";
    private static final String WINDOWS = "windows";
    private static final String SOLARIS = "SunOS";
    private static final String AS400 = "os/400";

    private static final String OS_NAME = "os.name";

    private static OsService oSService;

    public static Logger logger = LoggerFactory.getLogger(OsServiceFactory.class);

    public static OsService getOsService() {
        if (oSService != null) {
            return oSService;
        }

        
        
        
        String archCfg = null;

			archCfg = "mantis.conf";

		System.out.println("Archivo de Configuracion: " + archCfg);
		cfg = new Configurador(archCfg);
		
	
        // String debugOs = ServicioAgente.cfg.getDebugOs();
        // String sistemaOperativo = System.getProperty(OS_NAME);
        // sistemaOperativo = debugOs.contentEquals("") ? sistemaOperativo :
        // debugOs;
        String sistemaOperativo = checkOs();

        if (sistemaOperativo.toLowerCase().contains(WINDOWS)) {
            oSService = newWindowsService();
        } else if (sistemaOperativo.toLowerCase().contains(lINUX)) {
            oSService = new LinuxService();
        } else if (sistemaOperativo.toLowerCase().contains(UNIX)) {
            oSService = new UnixService();
//        } else if (sistemaOperativo.toLowerCase().contains(HPUX)) {
//            oSService = new HPUXService();
        } else if (sistemaOperativo.toLowerCase().contains(SOLARIS)) {
            if(cfg.getOsSolarisType().equals("A")) {
            	oSService = new SolarisService2();	
            }else {
            	oSService = new SolarisService();
            }
        	
            
        } else if (sistemaOperativo.toLowerCase().contains(AS400)) {        	
            oSService = new AS400Service();
        } else {
            oSService = new SolarisService();
        }

        return oSService;
    }

    public static OsService newWindowsService() {
        try {
            return (OsService) Class.forName("ast.servicio.probatch.os.service.impl.AclWindowsService").newInstance();
        } catch (Exception e) {
            logger.error("No se pudo instanciar AclWindowsService", e);
            return new WindowsService();
        }
    }

    //	public static String checkOs() {
    //		String debugOs = ServicioAgente.cfg.getDebugOs();
    //		String sistemaOperativo = System.getProperty(OS_NAME);
    //		sistemaOperativo = debugOs.contentEquals("") ? sistemaOperativo : debugOs;
    //		return sistemaOperativo.toLowerCase();
    //	}

    public static String checkOs() {
        String targetOs = ServicioAgente.cfg.getTarget_os();
        String sistemaOperativo = System.getProperty(OS_NAME);
        sistemaOperativo = targetOs.contentEquals("") ? sistemaOperativo : targetOs;
        return sistemaOperativo.toLowerCase();
    }

    public static boolean isWindowSO() {
        return checkOs().contains(WINDOWS);
    }

    public static boolean isLinuxSO() {
        return checkOs().equals(lINUX);
    }

    public static boolean isUnixSO() {
        return checkOs().equals(UNIX);
    }

    public static boolean isHPUXSO() {
        return checkOs().equals(HPUX);
    }

    public static boolean isSolarisSO() {
        return checkOs().equals(SOLARIS);
    }

    public static boolean isAS400SO() {
        return checkOs().equals(AS400);
    }
}

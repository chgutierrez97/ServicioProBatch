package ast.servicio.probatch.configuration;

import ast.servicio.probatch.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

public class Configurador {

    public static Logger logger = LoggerFactory.getLogger(Configurador.class);

    private int port;
    private String key;
    private String wrkdir;
    /* tiempo de limpieza de logs */
    private double timeout;
    private boolean do_authentication;
    private int heartbeat_interval;
    private int dump_interval;
    private int clean_logs;
    private String logfile;
    private int logdays;
    private int result_maxsize;
    private int output_maxsize;
    private int max_returned_log_size;
    private String dump_file;
    private String target_os;
    private String ignore_re;
    private String usuariosString;
    private String readerType;
    private String comandosString;
    private String winRunAs;
    private String runAsUser;
    // private int timeout_socket;
    private String shellScript;
    private String envCmd;
    private String debugOs = "";

    /* modo de debug a usar */
    private int debugMode = 0;
    private String debugDummyCmd = "";
    private String osSolarisType="";
    private int cmdEscapeType = 0;
    public static final int DEBUG_MODE_OFF = 0;
    public static final int DEBUG_RUN_CMD_NO_HEADERS = 1;
    public static final int DEBUG_RUN_SPEC_CMD_ONLY = 2;
    public static final int DEBUG_RUN_SU_CHDIR = 3;
    public static final int DEBUG_RUN_DEBUG_DUMMY_CMD = 4;

    private ArrayList<String> usuarios = new ArrayList<String>();
    private ArrayList<String> comandos = new ArrayList<String>();
    private String defTerminal = "/bin/sh";
    private String winScript;
    private String aS400Server;

    private String aS400User;

    private String aS400Pass;

    private Boolean validateWithJava;

    public Configurador(String archCfg) {
        logger.debug("leyendo configuraciones de " + new File(archCfg).getAbsolutePath());

        Properties prop = new Properties();
        InputStream is = null;

        try {
            is = new FileInputStream(archCfg);
            prop.load(is);
            try {
                this.cargarParametros(prop);
            } catch (Exception e) {
                logger.error("Error al leer el archivo de configuracion.");
                System.exit(0);
            } finally {
                is.close();
            }
        } catch (IOException ioe) {
            System.err.println("No se pudo leer archivo de configuracion.");
            logger.error("No se pudo leer archivo de configuracion.");
            logger.trace(ioe.getMessage());
            System.exit(6);
        }

        if (debugMode != 0) {
            logger.debug("Servicio Agente corriendo en modo Debug: " + debugMode);
        }
    }

    private void cargarParametros(Properties prop) {
        winRunAs = "./lib/WinRunAs.exe";
        runAsUser = "./lib/runAsUser.exe";

        port = Integer.parseInt(prop.getProperty("port", "6666"));
        key = prop.getProperty("key", "BqMjs40n");
        wrkdir = prop.getProperty("wrkdir", ".");
        /*
		 * Tiempo en horas que se guarda los estados de los procesos.(default
		 * 24). Valor minimo: 0.00028
		 */
        timeout = Double.parseDouble(prop.getProperty("timeout", "24"));
        do_authentication = Boolean.parseBoolean(prop.getProperty("do_authentication", "true"));
        heartbeat_interval = Integer.parseInt(prop.getProperty("heartbeat_interval", "30"));
        dump_interval = Integer.parseInt(prop.getProperty("dump_interval", "300"));
        clean_logs = Integer.parseInt(prop.getProperty("clean_logs", "300"));
        readerType = prop.getProperty("readerType", "0");
        logfile = prop.getProperty("logfile", "probatch.log");
        logdays = Integer.parseInt(prop.getProperty("logdays", "5"));
        result_maxsize = Integer.parseInt(prop.getProperty("result_maxsize", "56"));
        output_maxsize = Integer.parseInt(prop.getProperty("output_maxsize", "0"));
        max_returned_log_size = Integer.parseInt(prop.getProperty("max_returned_log_size", "2000"));
        dump_file = prop.getProperty("dump_file");
        ignore_re = prop.getProperty("ignore_re");
        usuariosString = prop.getProperty("usuarios");
        comandosString = prop.getProperty("comandos");
        setUsuarios(usuariosString);
        setComandos(comandosString);
        // timeout_socket = Integer.parseInt(prop.getProperty("timeout_socket",
        // "60000"));
        shellScript = prop.getProperty("shellScript", wrkdir + "/lib/ExecuteScript.sh");

        debugMode = Integer.parseInt(prop.getProperty("debugMode", "0"));

        debugDummyCmd = prop.getProperty("debugDummyCmd", "");
        osSolarisType = prop.getProperty("osSolarisType", ""); 
        cmdEscapeType = Integer.parseInt(prop.getProperty("cmdEscapeType", "0"));
        envCmd = prop.getProperty("envCmd", "");
        debugOs = prop.getProperty("debugOs", "");

        defTerminal = prop.getProperty("terminal", defTerminal);

        winScript = prop.getProperty("win_script");
        if (winScript != null) { logger.debug("Parametro de configuracion win_script detectado: {}", winScript); }

        target_os = prop.getProperty("target_os", System.getProperty("os.name"));

        aS400Server = prop.getProperty("as400_host", "localhost");

        aS400User = prop.getProperty("as400_user");

        aS400Pass = prop.getProperty("as400_pass");

        validateWithJava = Boolean.valueOf(prop.getProperty("validateWithJava", "false"));
    }

    public String getWinRunAs() {
        return winRunAs;
    }

    public void setWinRunAs(String winRunAs) {
        this.winRunAs = winRunAs;
    }

    public String getRunAsUser() {
        return runAsUser;
    }

    public void setRunAsUser(String runAsUser) {
        this.runAsUser = runAsUser;
    }

    public int getClean_logs() {
        return clean_logs;
    }

    public void setClean_logs(int cleanLogs) {
        clean_logs = cleanLogs;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getWrkdir() {
        return wrkdir;
    }

    public void setWrkdir(String wrkdir) {
        this.wrkdir = wrkdir;
    }

    public double getTimeout() {
        return timeout;
    }

    public void setTimeout(double timeout) {
        this.timeout = timeout;
    }

    public boolean isDo_authentication() {
        return do_authentication;
    }

    public boolean ignoreAuthentication() {
        return !isDo_authentication();
    }

    public void setDo_authentication(boolean do_authentication) {
        this.do_authentication = do_authentication;
    }

    public int getHeartbeat_interval() {
        return heartbeat_interval;
    }

    public void setHeartbeat_interval(int heartbeat_interval) {
        this.heartbeat_interval = heartbeat_interval;
    }

    public int getDump_interval() {
        return dump_interval;
    }

    public void setDump_interval(int dump_interval) {
        this.dump_interval = dump_interval;
    }

    public String getLogfile() {
        return logfile;
    }

    public void setLogfile(String logfile) {
        this.logfile = logfile;
    }

    public String getReaderType() {
        return readerType;
    }

    public void setReaderType(String readerType) {
        this.readerType = readerType;
    }

    public int getLogdays() {
        return logdays;
    }

    public void setLogdays(int logdays) {
        this.logdays = logdays;
    }

    public int getResult_maxsize() {
        return result_maxsize;
    }

    public void setResult_maxsize(int result_maxsize) {
        this.result_maxsize = result_maxsize;
    }

    public int getOutput_maxsize() {
        return output_maxsize * 1024;
    }

    public void setOutput_maxsize(int output_maxsize) {
        this.output_maxsize = output_maxsize;
    }

    public int getMax_returned_log_size() {
        return max_returned_log_size;
    }

    public void setMax_returned_log_size(int max_returned_log_size) {
        this.max_returned_log_size = max_returned_log_size;
    }

    public String getDump_file() {
        return dump_file;
    }

    public void setDump_file(String dump_file) {
        this.dump_file = dump_file;
    }

    public String getTarget_os() {
        return target_os;
    }

    public void setTarget_os(String target_os) {
        this.target_os = target_os;
    }

    public ArrayList<String> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(String usuariosString) {
        try {
            usuarios = (ArrayList<String>) Utils.obtenerCadenas(usuariosString, ",");
        } catch (Exception e) {
            logger.error("Surgieron errores al leer el atributo usuarios del archivo de configuracion. " + e.getMessage());
        }
    }

    public String getComandos(String comandoEntrada) {
        for (Iterator<String> iterator = this.comandos.iterator(); iterator.hasNext(); ) {
            String comando = (String) iterator.next();
            if (comando.contains(comandoEntrada)) {
                return comando;
            }

        }
        return comandoEntrada;
    }

    public void setComandos(String comandosString) {
        try {
            this.comandos = (ArrayList<String>) Utils.obtenerCadenas(comandosString, ",");
        } catch (Exception e) {
            logger.error("Surgieron errores al leer el atributo comandos del archivo de configuracion. " + e.getMessage());
        }
    }

    public String getIgnore_re() {
        return ignore_re;
    }

    public void setIgnore_re(String ignoreRe) {
        ignore_re = ignoreRe;
    }

    // public int getTimeout_socket() {
    // return timeout_socket;
    // }
    //
    // public void setTimeout_socket(int timeout_socket) {
    // this.timeout_socket = timeout_socket;
    // }

    public String getShellScript() {
        return shellScript;
    }

    public void setShellScript(String shellScript) {
        this.shellScript = shellScript;
    }

    /**
     * Habilita o deshabilita el modo debug.
     * <p>
     * 0 - Modo debug inhabilitado.
     *
     * @return Codigo de modo debug.
     */
    public int getDebugMode() {
        return debugMode;
    }

    public String getDebugDummyCmd() {
        return debugDummyCmd;
    }
    
    public String getOsSolarisType() {
        return osSolarisType;
    }
    
    public int getCmdEscapeType() {
        return cmdEscapeType;
    }

    public void setCmdEscapeType(int cmdEscapeType) {
        this.cmdEscapeType = cmdEscapeType;
    }

    public String getEnvCmd() {
        return envCmd;
    }

    /**
     * Retorna el sistema operativo de debug a setear. Si esta propiedad esta
     * activa, entonces se ignora el tipo de sistema operativo y se reemplaza
     * por este mismo.
     *
     * @return Sistema operativo debug.
     */
    public String getDebugOs() {
        return debugOs;
    }

    /**
     * Obtiene la terminal por defecto a invocar para correr el comando.
     *
     * @return terminal por defecto a invocar para correr el comando.
     */
    public String getDefTerminal() {
        return defTerminal;
    }

    /**
     * Obtiene el .EXE a utilizar para invocar los comandos de windows.
     *
     * @param defaultValue Valor por defecto a usar si acaso el parametro win_script no
     *                     fue configurado.
     * @return Path de .EXE a utilizar para ejecutar comandos de Windows.
     */
    public String getWinScript(String defaultValue) {
        if (winScript == null) { return defaultValue; }

        if (new File(winScript).exists()) { return winScript; }
        if (new File(String.format("%s.exe", winScript)).exists()) { return String.format("%s.exe", winScript); }

        if (new File(String.format("./lib/%s", winScript)).exists()) { return String.format("./lib/%s", winScript); }
        if (new File(String.format("./lib/%s.exe", winScript)).exists()) { return String.format("./lib/%s.exe", winScript); }

        throw new IllegalStateException(String.format("El valor '%s' del parametro win_script es invalido!", winScript));
    }

    public String getaS400Server() {
        return aS400Server;
    }

    public String getaS400User() {
        return aS400User;
    }

    public String getaS400Pass() {
        return aS400Pass;
    }

    public Boolean getValidateWithJava() {
        return validateWithJava;
    }
}

package ast.servicio.probatch.os.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;

import ast.servicio.probatch.configuration.Configurador;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;

public abstract class OsService {

    public static Logger logger = LoggerFactory.getLogger(OsService.class);

    public String idParametroProceso;

    public abstract int getPid(java.lang.Process proceso) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
            AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException;

    public abstract boolean is_absolute_path(String x);

    /**
     * Rodea con comillas simples a un String.
     *
     * @param s - String a rodear con comillas simples.
     * @return Strin rodeado con comillas simples.
     */
    public static String quote(String s) {
        return "'" + s + "'";
    }

    /**
     * Rodea con comillas dobles a un String.
     *
     * @param s - String a rodear con comillas dobles.
     * @return Strin rodeado con comillas dobles.
     */
    public static String doubleQuote(String s) {
        return "\"" + s + "\"";
    }

    /**
     * Se crea un comando a ejecutar para un sistema operativo especifico.
     *
     * @param parametroP - Informacion sobre comando a ejecutar.
     * @return Arreglo de strings de comandos generados.
     * @throws Exception
     */
    public abstract String[] getExecuteCommand(ParametrosProceso parametroP) throws Exception;

    /**
     * Retorna el string comando a invocar para matar a un proceso.
     *
     * @param pid - Process id del proceso a matar.
     * @return string comando a invocar para matar a un proceso.
     */
    public abstract String getKillCommand(int pid);

    public abstract char getCaracterBarra();

    public String reemplazaExpRegArchivo(String nombre) {
        logger.debug("reemplazaExpRegArchivo::nombre = {}", nombre);

        File arch = new File(nombre);
        if (arch.isDirectory()) {
            return nombre;
        }

        String nombreArchivo = arch.getName();
        final String regex = nombreArchivo.replace("?", ".?").replace("*", ".*?");
        File dir = arch.getParentFile();
        if (!dir.exists()) {
            return nombre;
        }

        File[] archivos = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.matches(regex);
            }
        });

        String res = archivos == null || archivos.length == 0 ? nombre : Utils.ultimoModificado(archivos);
        logger.debug("reemplazaExpRegArchivo::res = {}", res);
        return res;
    }

    /*
     * [./lib/runAsUser.exe, c:\procesos, martin.zaragoza, Marzo2015,
     * "C:\WINDOWS\system32\cmd.exe", "/c ComandoImprimeLineas.exe 20 2"]
     * entorno=[] dirEjecucion=c:\procesos
     *
     * FIXME: funciona la invocacion del comando con debug mode 8. Sin embargo
     * siempre termina con estado falla.
     */
    public Process executeCommand(String[] cmd, String[] entorno, File dirEjecucion)
            throws IOException, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, ObjectDoesNotExistException {

        // if (debugMode == 8) {
        // String stringCmd = cmd[0] + " " + dirEjecucion.getAbsolutePath() + "
        // " + cmd[2] + " " + cmd[3] + " " + cmd[4] + " " + cmd[5];
        // return Runtime.getRuntime().exec(stringCmd);
        // }
        //
        // if (debugMode == 10) {
        // return Runtime.getRuntime().exec(cmd, null, null);
        // }

        String[] printCmd = new String[cmd.length];
        /*
         * Copio el arreglo del comando y modifico el password para que no se
		 * imprima en el log
		 */
        System.arraycopy(cmd, 0, printCmd, 0, cmd.length);
        if (printCmd.length > 3) {
            printCmd[3] = "****";
        }

        List<String> envars = new ArrayList<String>(Arrays.asList(entorno));
        // envars.add(String.format("%s=%s", Configurador.PBATCHDIR,
        // System.getProperty(Configurador.PBATCHDIR)));
        // return Runtime.getRuntime().exec(cmd, envars.toArray(new String[0]),
        // dirEjecucion);

        Map<String, String> sysEnv = System.getenv();
        Set<Map.Entry<String, String>> envEntries = sysEnv.entrySet();
        for (Map.Entry<String, String> entry : envEntries) {
            if (!containsEnvar(envars, entry.getKey())) {
                envars.add(entry.getKey() + "=" + entry.getValue());
            }
        }
        logger.info(String.format("EJECUTANDO: %s | %s | %s", Arrays.toString(printCmd), envars, dirEjecucion));
        // return Runtime.getRuntime().exec(cmd, envArray, dirEjecucion);
        return Runtime.getRuntime().exec(cmd, envars.toArray(new String[0]), dirEjecucion);

    }// executeCommand

    public Process executeCommand(String comandoCmd, String[] entorno, File dirEjecucion) throws IOException {
        return Runtime.getRuntime().exec(comandoCmd, entorno, dirEjecucion);
    }

    public Process executeCommand(String[] comandoCmd) throws IOException {
        return Runtime.getRuntime().exec(comandoCmd);
    }

    public Process executeCommand(String comandoCmd) throws IOException {
        String logHeader = this.getClass().getName() + "::executeCommand: ";

        if (ServicioAgente.cfg.getDebugMode() == Configurador.DEBUG_RUN_DEBUG_DUMMY_CMD) {
            logger.debug(logHeader + "corriendo modo run debug dummy cmd");

            String debugDummyCmd = ServicioAgente.cfg.getDebugDummyCmd();
            if (debugDummyCmd != null && debugDummyCmd.contentEquals("") == false) {
                comandoCmd = ServicioAgente.cfg.getDebugDummyCmd();
            } else {
                logger.debug(logHeader + "debugDummyCmd es vacio o null");
            }
        }

        logger.debug(logHeader + "a punto de ejecutar=" + comandoCmd);
        return Runtime.getRuntime().exec(comandoCmd);
    }// executeCommand

    public String executeCommandOutputString(String[] comandos) throws MensajeErrorException {
        StringBuffer ret = new StringBuffer();
        BufferedReader in;
        String line;

        try {
            Process proc = executeCommand(comandos);

            String com = "";

            for (int i = 0; i < comandos.length; i++) {
                com = com + comandos[i];
            }
            // logger.debug(com);

            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            while ((line = in.readLine()) != null) {

                ret.append(line + "\n");
            }

        } catch (Exception e) {
            String comandosString = new String();
            for (int i = 0; i < comandos.length; i++) {
                comandosString = comandosString + comandos[i];
            }

            throw new MensajeErrorException("Error al ejecutar el comando - " + "[" + comandosString + "]");
        }
        // logger.debug(ret.toString());
        return ret.toString();
    }

    private boolean containsEnvar(List<String> envars, String varname) {
        for (String envar : envars) {
            if (envar.toLowerCase().startsWith(varname.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean getEquivalenciasPermisos(String permissionsToCheck, String usrPermissions) {
        logger.debug(String.format("getEquivalenciasPermisos::permissionsToCheck=%s , usrPermissions=%s", permissionsToCheck, usrPermissions));

        // boolean validacion = false;
        // String permisosAVerificarMinuscula =
        // permissionsToCheck.toLowerCase();
        // String permisosDevueltosMinuscula = usrPermissions.toLowerCase();
        // String cadAuxiliar = "";
        // for (int i = 0; i < permissionsToCheck.length(); i++) {
        // if (permisosAVerificarMinuscula.charAt(i) == 'r' &&
        // permisosDevueltosMinuscula.contains("r") &&
        // !cadAuxiliar.contains("r")) {
        // validacion = true;
        // cadAuxiliar = cadAuxiliar + "r";
        // } else if (permisosAVerificarMinuscula.charAt(i) == 'x' &&
        // permisosDevueltosMinuscula.contains("x") &&
        // !cadAuxiliar.contains("x")) {
        // validacion = true;
        // cadAuxiliar = cadAuxiliar + "x";
        // } else if (permisosAVerificarMinuscula.charAt(i) == 'w' &&
        // permisosDevueltosMinuscula.contains("w") &&
        // !cadAuxiliar.contains("w")) {
        // validacion = true;
        // cadAuxiliar = cadAuxiliar + "w";
        // } else {
        // validacion = false;
        // break;
        // }
        //
        // logger.debug(String.format("getEquivalenciasPermisos::cadAuxiliar=%s",
        // cadAuxiliar));
        // }
        //
        // return validacion;

		/* DIVIDO EL STRING DE PERMISOS "rwx" EN [,r,w,x] USANDO SPLIT */
        String[] splitPermissionsToCheck = permissionsToCheck.split("");
        for (String permissionToCheck : splitPermissionsToCheck) {
            if (!usrPermissions.toLowerCase().contains(permissionToCheck.toLowerCase())) {
                return false;
            }
        }

        return true;
    }

    public String resuelveVariablesDeSistema(String mensaje, Pattern pattern) {
        Matcher matcher = pattern.matcher(mensaje);

        String outMsg = mensaje;
        while (matcher.find()) {
            if (matcher.groupCount() >= 1) {
                String fullVarName = matcher.group(0);
                String varName = matcher.group(1);
                String varValue = System.getenv(varName);

                if (varValue != null && !"".equals(varValue)) {
                    outMsg = outMsg.replaceAll(Pattern.quote(fullVarName), Matcher.quoteReplacement(varValue));
                }
            }
        }

        return outMsg;
    }
    
    
    public boolean dirOrFileExists(String path) {
    	return Utils.validarExistenciaArchivo(path);
		
    	
    }

    /**
     * Valida si un usuario posee ciertos permisos (y con la posibilidad, en
     * windows, de incluir dominio) en un determinado path (archivo,directorio)
     *
     * @param usuario
     * @param permisos
     * @param path
     * @param dominio
     * @return
     * @throws MensajeErrorException
     */
    public abstract boolean buscarUsuarioPermisos(String usuario, String permisos, String path, String dominio) throws MensajeErrorException;

    public abstract String resuelveVariablesDeSistema(String mensaje);

    public abstract String escapaSaltosDeLinea(String cadena);

}

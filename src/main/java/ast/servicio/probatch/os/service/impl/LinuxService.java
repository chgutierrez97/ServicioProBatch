package ast.servicio.probatch.os.service.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.domain.UsuarioPermiso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.os.service.OsService;
import ast.servicio.probatch.util.StringCommands;
import ast.servicio.probatch.util.Utils;

public class LinuxService extends OsService {
    public static Logger logger = LoggerFactory.getLogger(LinuxService.class);

    public LinuxService() {
    }

    public int getPid(Process proceso) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        int pid = 0;
        if (proceso.getClass().getName().equals("java.lang.UNIXProcess")) {
			/* get the PID on unix/linux systems */
            Field f = proceso.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            pid = f.getInt(proceso);
        }
        return pid;
    }

    @Override
    public String[] getExecuteCommand(ParametrosProceso parametroP) throws Exception {

        StringBuilder parametros = new StringBuilder();

        // su - $USER
        parametros.append(StringCommands.IMPERSONALIZATION_USER_FULL.toString() + StringCommands.HYPHEN.toString());
        parametros.append(parametroP.getUsuario().getNombre());
        parametros.append(StringCommands.STRING_EMPTY.toString());

        // su - $USER -c '
        parametros.append(StringCommands.COMMAND_OPTION.toString());
        parametros.append(StringCommands.STRING_EMPTY.toString());
        parametros.append(StringCommands.QUOTE_SIMPLE.toString());

        
        // su - $USER -c 'cd $CHDIR;
        
        parametros.append(StringCommands.CHDIR.toString());
        parametros.append(StringCommands.STRING_EMPTY.toString());
        parametros.append(parametroP.getChdir());
        parametros.append(StringCommands.PUNTO_COMA);
        parametros.append(StringCommands.STRING_EMPTY.toString());
        
        // su - $USER -c 'cd $CHDIR; export $KEY1=$VAR1 $KEY2=$VAR2 ... ;
        if (parametroP.getEntorno() != null && !parametroP.getEntorno().isEmpty()) {
            parametros.append(StringCommands.EXPORT.toString());
            Collection<Atributo> varsEntorno = parametroP.getEntorno();
            int varCount = varsEntorno.size();
            int varIndex = 0;
            for (Atributo varEntorno : varsEntorno) {
                parametros.append(varEntorno.getNombre() + "=" + varEntorno.getValorMostrar());
                if (varIndex++ < varCount - 1) {
                    parametros.append(StringCommands.STRING_EMPTY);
                }
            }
            parametros.append(StringCommands.PUNTO_COMA);
        }

        // su - $USER -c 'cd $CHDIR; export $KEY1=$VAR1 $KEY2=$VAR2 ... ; $CMD
        parametros.append(parametroP.getComando());
		/* parametros.append(StringCommands.STRING_EMPTY); */

        // su - $USER -c 'cd $CHDIR; export $KEY1=$VAR1 $KEY2=$VAR2 ... ; $CMD $ARGS...'
        if (parametroP.getArgumentos() != null) {
            for (Iterator<Atributo> iterator = parametroP.getArgumentos().iterator(); iterator.hasNext(); ) {
                Atributo atributo = iterator.next();
                parametros.append(StringCommands.STRING_EMPTY.toString());
                parametros.append(atributo.getValor());
            }
        }
        parametros.append(StringCommands.QUOTE_SIMPLE.toString());

        // /bin/sh -c
        String[] comando = { StringCommands.COMMAND_MAIN.toString(), StringCommands.COMMAND_OPTION.toString(), parametros.toString() };
        // String[] comando = { parametros.toString() };

        logger.info("COMANDO CONSTRUIDO: " + Arrays.toString(comando));

        return comando;

        // las siguientes instrucciones comentadas solo funcionan en UBUNTU y
        // CENTOS

        /**
         * int indx = parametroP.getArgumentos().size(); String[] retorno = new
         * String[indx + 4]; int i = 0; retorno[i] = "sudo"; i++; retorno[i] =
         * "-u"; i++; retorno[i] = parametroP.getUsuario().getNombre(); i++;
         * retorno[i] = parametroP.getComando();
         *
         * for (Iterator<Atributo> iterator =
         * parametroP.getArgumentos().iterator(); iterator.hasNext();) {
         * Atributo atributo = iterator.next(); i++; retorno[i] =
         * atributo.getValor(); } return retorno;
         */

    }

    @Override
    public String getKillCommand(int pid) {
        return "pkill -TERM -P " + pid;
    }

    @Override
    public boolean buscarUsuarioPermisos(String usuario, String permisos, String path, String dominio) throws MensajeErrorException {
        // logger.debug("Buscar permisos de usuasrio");
        boolean resultado = false;
        if (usuario.equals("root")) {
            if (getEquivalenciasPermisos(permisos, "rwx")) {
                resultado = true;
            }
        }
        // logger.debug(usuario);
        if (existeUsuario(usuario)) {
            // logger.debug(existeUsuario(usuario) + "");
            if (usuario.equalsIgnoreCase(permisosUsuario(path).get(0).getUsuario())) {
                if (getEquivalenciasPermisos(permisos, permisosUsuario(path).get(0).getPermisos())) {
                    resultado = true;
                    // logger.debug("" + resultado);
                }
            } else if (buscarGrupo(usuario, permisosUsuario(path).get(1).getUsuario())) {
                if (getEquivalenciasPermisos(permisos, permisosUsuario(path).get(1).getPermisos())) {
                    resultado = true;
                    // logger.debug("" + resultado);
                }
            } else {
                if (getEquivalenciasPermisos(permisos, permisosUsuario(path).get(2).getPermisos())) {
                    resultado = true;
                    // logger.debug("" + resultado);
                }
            }
        }
        // logger.debug("" + resultado);
        return resultado;
    }

    /**
     * Arma una lista de UsuarioPermiso, con los usuarios y grupos y sus
     * determinados permisos en un cierto path
     *
     * @param path
     * @return
     * @throws MensajeErrorException
     */
    private List<UsuarioPermiso> permisosUsuario(String path) throws MensajeErrorException {

        // logger.debug("permisosUsuario");
        List<UsuarioPermiso> usuarioPermisos = new ArrayList<UsuarioPermiso>();
        // String aux = ejecutarComandoLinux("ls -al ", path);
        String aux = executeCommandOutputString(new String[] { "ls", "-als", path });
        String usuario;
        String permiso;

        // Siempre va a devolver una linea sola: Ejemplo
        // -rw-r--r-- 1 ubuntu ubuntu 56054 2012-03-09 08:35
        // /home/ubuntu/Downloads/Validar3.jar
        if (aux != null) {
            if (aux.startsWith("total")) {
                aux = aux.substring(aux.indexOf('\n') + 1, aux.length());

            }
            permiso = aux.substring(1, 4);
            permiso.replaceAll("-", "");
            usuario = Utils.obtenerTerceraCad(aux, " ");
            usuarioPermisos.add(new UsuarioPermiso(usuario, permiso, ""));
            permiso = aux.substring(4, 7);
            usuario = Utils.obtenerCuartaCad(aux, " ");
            permiso = permiso.replaceAll("-", "");
            usuarioPermisos.add(new UsuarioPermiso(usuario, permiso, ""));
            permiso = aux.substring(7, 10);
            permiso = permiso.replaceAll("-", "");
            usuario = "users";
            usuarioPermisos.add(new UsuarioPermiso(usuario, permiso, ""));
        }
        // logger.debug(usuarioPermisos.size() + "");
        return usuarioPermisos;
    }

    /**
     * Valida si un usuario pertenece a un determinado grupo
     *
     * @param usuario
     * @param grupo
     * @return
     * @throws MensajeErrorException
     */
    private boolean buscarGrupo(String usuario, String grupo) throws MensajeErrorException {
        String grupos = executeCommandOutputString(new String[] { "groups", usuario });
        boolean resultado = false;
        if (grupos.indexOf(grupo) > 0) { resultado = true; }
        return resultado;

    }

    /**
     * Valida si el usuario es un usuario correcto del sistema.
     *
     * @param usuario
     * @return
     * @throws MensajeErrorException
     */
    private boolean existeUsuario(String usuario) throws MensajeErrorException {

        if (executeCommandOutputString(new String[] { "id", usuario }).length() > 30) {
            // logger.debug("true");
            return true;
        } else {
            // logger.debug("false");
            return false;
        }
    }

    @Override
    public boolean is_absolute_path(String x) {
        // TODO Auto-generated method stub
        return false;
    }

    public char getCaracterBarra() {
        return '/';
    }


    public String resuelveVariablesDeSistema(String mensaje) {
        Pattern pattern = Pattern.compile("\\$(\\w+)");
        return resuelveVariablesDeSistema(mensaje, pattern);
    }

    public String escapaSaltosDeLinea(String cadena) {

        return StringEscapeUtils.escapeXml(cadena).replaceAll("" + (char) 10, "#x0a;");
    }

}

package ast.servicio.probatch.os.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Pointer;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.domain.UsuarioPermiso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.message.MensajeValidacion;
import ast.servicio.probatch.os.service.OsService;
import ast.servicio.probatch.os.service.util.Kernel32;
import ast.servicio.probatch.os.service.util.W32API;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;

public class WindowsService extends OsService {
	public static Logger logger = LoggerFactory.getLogger(MensajeValidacion.class);

	public WindowsService() {
	}

	@Override
	public boolean is_absolute_path(String x) {
		Pattern absolutePath = Pattern.compile("^[a-z]\\:[\\/].*");
		Matcher absoluteMatcher = absolutePath.matcher(x);

		return absoluteMatcher.matches();
	}

	@Override
	public int getPid(Process proceso) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field f = proceso.getClass().getDeclaredField("handle");
		f.setAccessible(true);
		long handl = f.getLong(proceso);

		Kernel32 kernel = Kernel32.INSTANCE;
		W32API.HANDLE handle = new W32API.HANDLE();
		handle.setPointer(Pointer.createConstant(handl));
		return kernel.GetProcessId(handle);
	}

	@Override
	public String[] getExecuteCommand(ParametrosProceso parametroP) throws Exception {

		String atributos = "";
		ArrayList<String> listaParam = new ArrayList<String>();
		String[] comandoAsArray;
		String comando = parametroP.getComando();

		/* CON MODO DEBUG == 10, EL COMANDO SE INTENTA EJECUTAR USANDO psexec */
		// int i = 0;
		// int debugMode = ServicioAgente.cfg.getDebugMode();

		// switch (debugMode) {
		// case 10:
		// listaParam.add("psexec");
		// listaParam.add("-u");
		// listaParam.add("ACCUSYSARGBSAS\\martin.zaragoza");
		// listaParam.add("-p");
		// listaParam.add("Marzo2015");
		//
		// listaParam.add(parametroP.getUsuario().getValor());
		// listaParam.add(parametroP.getChdir() + "\\" + getComando);
		//
		// if (parametroP.getArgumentos() != null) {
		// for (Iterator<Atributo> iterator =
		// parametroP.getArgumentos().iterator(); iterator.hasNext();) {
		// Atributo atributo = iterator.next();
		// listaParam.add(atributo.getValor());
		// }
		// }
		// comandoAsArray = listaParam.toArray(new String[listaParam.size()]);
		//
		// default:

		listaParam.add(ServicioAgente.winExecAs);
		listaParam.add(parametroP.getChdir());

		listaParam.add(parametroP.getUsuario().getNombre());

		listaParam.add(parametroP.getUsuario().getValor());
		listaParam.add(doubleQuote(comando));

		/*
		 * Para ejecutar un comando de sistema operativo, se suele enviar en la trama de
		 * entrada el elemento <command>C:\System32\CMD.EXE</command> o
		 * <command>cmd</command>. Para que windows ejecute correctamente un comando, es
		 * necesario parar como primer argumento /c.
		 */
		if (!comando.toLowerCase().contains("sqlcmd"))
			if (comando.toLowerCase().equals("cmd") || comando.toLowerCase().contains("cmd.exe")) {
				atributos += "/c";
			}

		if (parametroP.getArgumentos() != null) {
			for (Atributo atributo : parametroP.getArgumentos()) {
				String valorAtributo = atributo.getValor();

				valorAtributo = valorAtributo.replaceAll("\"", "\\\\\"");
				// valorAtributo = valorAtributo.replaceAll("'", "\\\\\"");

				/*
				 * SI EL ARGUMENTO ES COMPUESTO, ENTONCES EL MISMO DEBE ESCAPEARSE CON \ Y LUEGO
				 * ". ESTO SE DEBE A QUE EL COMANDO SE CORRERA USANDO runAsUser.exe DESARROLLADO
				 * POR ACCUSYS EN C++. PARA QUE LOS ARGUMENTOS SE PASEN CORRECTAMENTE, SE DEBE
				 * REALIZAR UN ESCAPEO ESPECIAL DE LOS MISMOS.
				 */
				/*
				 * SI AL ATRIBUTO LE ASIGNARON COMILLAS SIMPLES O DOBLES, SE LAS REMUEVO PARA
				 * AGREGARLE EL ESCAPEO ANTES MENCIONADO.
				 */
				// if (valorAtributo.contains("\"") ||
				// valorAtributo.contains("'")) {
				// valorAtributo = valorAtributo.replaceAll("\"", "");
				// valorAtributo = valorAtributo.replaceAll("'", "");
				// valorAtributo = "\\\"" + valorAtributo + "\\\"";
				// }
				atributos += " " + valorAtributo;
			}
		}

		listaParam.add(atributos);
		comandoAsArray = listaParam.toArray(new String[0]);

		// }

		// logger.debug("comandoAsArray=" + Arrays.toString(comandoAsArray));
		return comandoAsArray;
	}// getExecuteCommand

	@Override
	public String getKillCommand(int pid) {
		return "TASKKILL /F /T /PID " + pid;
	}

	@Override
	public boolean buscarUsuarioPermisos(String usuario, String permisos, String path, String dominio) throws MensajeErrorException {
		logger.debug(String.format("buscarUsuarioPermisos::usuario=%s, permisos=%s, dominio=%s", usuario, permisos, dominio));

		List<UsuarioPermiso> permisosUsuario = ServicioAgente.cfg.getValidateWithJava() ? getPermisosUsuarioJava(path) : getPermisosUsuario(path);
		// List<UsuarioPermiso> permisosUsuario = getPermisosUsuarioJava(path);

		logger.debug("Lista permisos usuario size = " + permisosUsuario.size());

		int indexOfElement = permisosUsuario.indexOf(new UsuarioPermiso(usuario, permisos, dominio));

		if (indexOfElement >= 0) {
			UsuarioPermiso usrPer = permisosUsuario.get(indexOfElement);
			if (getEquivalenciasPermisos(permisos, usrPer.getPermisos())) {
				if (usrPer.getDominio().equalsIgnoreCase("BUILTIN") || usrPer.getDominio().equalsIgnoreCase(dominio)) {
					logger.debug("buscarUsuarioPermisos::SALIDA1");
					return true;
				}
			}
		}
		for (UsuarioPermiso usuarioPermiso : permisosUsuario) {
			logger.debug("buscarUsuarioPermisos::checking " + usuarioPermiso);

			String iusr = usuarioPermiso.getUsuario();
			String iperm = usuarioPermiso.getPermisos();
			String idom = usuarioPermiso.getDominio();

			if (idom.equalsIgnoreCase("BUILTIN")) {
				// administradores o usuarios
				for (UsuarioPermiso usuarioPermisoNetLocalGroup : listaNetLocalGroup(iusr)) {
					if (usuarioPermisoNetLocalGroup.getUsuario().equalsIgnoreCase(usuario)
							&& usuarioPermisoNetLocalGroup.getDominio().equalsIgnoreCase(dominio)) {
						if (getEquivalenciasPermisos(permisos, iperm)) {
							logger.debug("buscarUsuarioPermisos::SALIDA2");
							return true;
						}
					} else if (dominio.equalsIgnoreCase(usuarioPermisoNetLocalGroup.getDominio())) {
						if (perteneceGrupoDominio(usuarioPermisoNetLocalGroup.getUsuario(), usuarioPermisoNetLocalGroup.getDominio(), usuario)) {
							if (getEquivalenciasPermisos(permisos, iperm)) {
								logger.debug("buscarUsuarioPermisos::SALIDA3");
								return true;
							}
						}
					}

				}
			}
		}
		return false;
	}

	/**
	 * Devuelve una lista de objetos del tipo "UsuarioPermiso", en los cuales se
	 * incluye usuario,permisos y dominio de un determinado archivo o directorio.
	 *
	 * @param path
	 *            Path de archivo o directorio del cual obtener los permisos.
	 * @return Conjunto de permisos de usuario del archivo.
	 * @throws MensajeErrorException
	 *             Si ocurrio un error al ejecutar el comando.
	 */
	protected List<UsuarioPermiso> getPermisosUsuarioJava(String path) throws MensajeErrorException {
		return new ArrayList<UsuarioPermiso>();
	}

	/**
	 * Devuelve una lista de objetos del tipo "UsuarioPermiso", en los cuales se
	 * incluye usuario,permisos y dominio de un determinado archivo o directorio.
	 *
	 * @param path
	 *            Path de archivo o directorio del cual obtener los permisos.
	 * @return Conjunto de permisos de usuario del archivo.
	 * @throws MensajeErrorException
	 *             Si ocurrio un error al ejecutar el comando.
	 */
	protected List<UsuarioPermiso> getPermisosUsuario(String path) throws MensajeErrorException {
		try {
			logger.debug("Obteniendo los permisos de usuario via SO");
			path = removeLastBackslashFromPath(path);

			Process process = executeCommand(new String[] { "cmd", "/c", "cacls", path });

			BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			// Se lee la primera linea
			String hostName = getHostName();

			List<UsuarioPermiso> usuarioPermisos = new ArrayList<UsuarioPermiso>();
			String line = "";

			boolean firstLineRead = false;
			while ((line = processOutputReader.readLine()) != null) {

				/*
				 * LA PRIMER LINEA CONTIENE EL PATH DE DIRECTORIO. POR ESO LA ELIMINAMOS
				 */

				if (!firstLineRead) {
					line = line.replaceFirst("(?i)" + Pattern.quote(path), "");
					firstLineRead = true;
				}

				line = line.trim();
				String[] splitLine = line.split(Pattern.quote("\\"));
				logger.debug("getPermisosUsuario::splitLine=" + Arrays.toString(splitLine));
				// System.out.println("getPermisosUsuario::splitLine=" +
				// Arrays.toString(splitLine));

				if (splitLine.length > 1) {
					String userAndPermissions = splitLine[1];
					String domain = splitLine[0];

					/* POR QUE SE HACE ESTO? */
					domain = domain.substring(domain.indexOf(" ") + 1, domain.length()).trim();

					logger.debug(String.format("getPermisosUsuario::userAndPermissions=%s | domain=%s", userAndPermissions, domain));
					// System.out.println(String.format("getPermisosUsuario::userAndPermissions=%s
					// | domain=%s", userAndPermissions, domain));

					// if (line.contains("\\")) {
					// usarioPermiso = Utils.obtenerUltimoSegmento(line, "\\");
					// dominio = Utils.obtenerCadIzquierda(line, "\\");
					// dominio = dominio.substring(dominio.indexOf(" ") + 1,
					// dominio.length());
					// dominio = dominio.trim();
					// }

					/* POR QUE SE HACE ESTO?? */
					domain = domain.equalsIgnoreCase("NT AUTHORITY") || domain.equalsIgnoreCase(hostName) ? "" : domain;

					// if (domain.equalsIgnoreCase("NT AUTHORITY") ||
					// domain.equalsIgnoreCase(hostName))
					// domain = "";

					String[] splitUserAndPermissions = userAndPermissions.split(Pattern.quote(":"));
					logger.debug("getPermisosUsuario::splitUserAndPermissions=" + Arrays.toString(splitUserAndPermissions));
					// System.out.println("getPermisosUsuario::splitUserAndPermissions="
					// + Arrays.toString(splitUserAndPermissions));

					String username = splitUserAndPermissions[0];
					String permissions = splitUserAndPermissions[1];
					permissions = Utils.obtenerUltimoSegmento(permissions, ")");
					permissions = permissions.replaceAll(" ", "");

					logger.debug(String.format("getPermisosUsuario::username=%s | permissions=%s", username, permissions));
					// System.out.println(String.format("getPermisosUsuario::username=%s
					// | permissions=%s", username, permissions));
					/*
					 * SEGUN LA DOCUMENTACION DEL COMANDO CACLS, LOS PERMISOS SON: N None, R Read, W
					 * Write, C Change (write), F Full control.
					 */

					permissions = permissions.equalsIgnoreCase("F") || permissions.equalsIgnoreCase("M") ? "rxw" : permissions;
					permissions = permissions.equalsIgnoreCase("C") ? "rw" : permissions;

					logger.debug(String.format("Permisos %s detectados para usuario %s de dominio %s", permissions, username, domain));
					// System.out.println(String.format("Permisos %s detectados
					// para usuario %s de dominio %s", permissions, username,
					// domain));
					usuarioPermisos.add(new UsuarioPermiso(username, permissions, domain));
				}
			}

			int retVal = process.waitFor();
			logger.debug("getPermisosUsuario::cacls return value: {}", retVal);
			// System.out.println("getPermisosUsuario::cacls return value: " +
			// retVal);

			return usuarioPermisos;
		} catch (Exception e) {
			throw new MensajeErrorException("Error al ejecutar el comando de validacion de permisos.");
		}
	}

	/**
	 * Elimina la ultima barra invertida del Path en caso que exista C:\Dir\ C:\Dir
	 *
	 * @param path
	 * @return
	 */

	protected String removeLastBackslashFromPath(String path) {
		path = path.trim();
		int idx = path.lastIndexOf("\\");
		if (idx + 1 == path.length()) {
			logger.debug("Quitando ultimo backslash del path " + path);
			path = new StringBuilder(path).replace(idx, idx + 1, "").toString();
			logger.debug("Ultimo backslash quitado " + path);
		}
		return path;
	}

	/**
	 * Devuelve una lista de objetos del tipo "UsuarioPermiso", en los cuales se
	 * incluye usuarios, grupos de usuario, permisos y dominios de un determinado
	 * grupo, el cual ademas de poseer usuarios, puede poseer tambien subgrupos.
	 *
	 * @param parametro
	 * @return
	 * @throws MensajeErrorException
	 */
	private List<UsuarioPermiso> listaNetLocalGroup(String parametro) throws MensajeErrorException {
		try {
			Process p = executeCommand(new String[] { "cmd", "/c", "net", "localgroup", parametro });
			InputStream is = p.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			// String line = br.readLine();
			if (br.readLine() == null) {
				throw new IOException();
			}

			boolean lineBreakFound = false;
			while (!lineBreakFound) {
				lineBreakFound = br.readLine().contains("----------------");
			}

			// while (!line.contains("----------------") && line != null) {
			// line = br.readLine();
			// }
			// if (line != null) {
			// line = br.readLine();
			// }

			List<UsuarioPermiso> usuarioPermisos = new ArrayList<UsuarioPermiso>();
			String line;

			/*
			 * LAS LINEAS TIENEN EL FORMATO Domain\Username, POR EJEMPLO:
			 * "ACCUSYSARGBSAS\Tecno_Admin"
			 */
			while ((line = br.readLine()) != null) {
				logger.debug("listaNetLocalGroup::line=" + line);

				String domain = "";
				String username = "";

				if (line.contains("\\")) {
					String[] lineSplit = line.split(Pattern.quote("\\"));
					domain = lineSplit[0];
					username = lineSplit[1];
				} else {
					username = line;
				}

				UsuarioPermiso usuarioPermiso = new UsuarioPermiso(username, domain);
				logger.debug("listaNetLocalGroup::" + usuarioPermiso);
				usuarioPermisos.add(usuarioPermiso);
			}

			/*
			 * ELIMINO LOS DOS ULTIMOS USUARIOS DE LA LISTA DADO QUE SON DATOS BASURA
			 * RESULTADO DE LAS ULTIMAS DOS LINEAS DE LA SALIDA DEL COMANDO
			 */
			usuarioPermisos.remove(usuarioPermisos.size() - 1);
			usuarioPermisos.remove(usuarioPermisos.size() - 1);

			return usuarioPermisos;
		} catch (IOException e) {
			throw new MensajeErrorException("Error al ejecutar el comando de validacion de permisos.");
		}
	}

	/**
	 * Valida si un determinado usuario pertenece a un determinado grupo en un
	 * cierto dominio, asi como tambien se esta validando la posibilidad de que el
	 * grupo ingresado como parametro no sea grupo.
	 *
	 * @param grupoAVerificar
	 * @param dominioAVerificar
	 * @param usuario
	 * @return
	 */
	private boolean perteneceGrupoDominio(String grupoAVerificar, String dominioAVerificar, String usuario) {
		boolean resultado = false;
		if (getDominioSistema().equalsIgnoreCase(dominioAVerificar)) {
			try {

				Process p2 = executeCommand(
						new String[] { "cmd", "/c", "net", "group", grupoAVerificar, "/Domain", "|", "findstr", "/R", "\"\\<" + usuario + "\\>\"" });

				InputStream is = p2.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String aux = br.readLine();
				int exitValue = p2.waitFor();
				if (exitValue == 0 && aux != null) {

					resultado = analizarResultadoFindStr(aux, usuario);
				}

			} catch (IOException e) {
				logger.error("Error al intentar verificar si " + usuario + " pertenece al dominio: " + dominioAVerificar + e.getMessage());
				logger.trace(e.getMessage());
			} catch (InterruptedException ie) {
				logger.error("Error al intentar verificar si " + usuario + " pertenece al dominio: " + dominioAVerificar + ie.getMessage());
				logger.trace(ie.getMessage());

			}
		}

		return resultado;
	}

	/**
	 * Valida si el usuario que se busca se encuentra en el resultado del findstr
	 *
	 * @param cadenaUsuarios
	 * @param usuarioBuscado
	 * @return
	 */
	private boolean analizarResultadoFindStr(String cadenaUsuarios, String usuarioBuscado) {
		StringTokenizer stk = new StringTokenizer(cadenaUsuarios, " ");
		while (stk.hasMoreElements()) {
			String usuario = stk.nextToken().trim();
			if (usuario.equalsIgnoreCase(usuarioBuscado)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Obtiene el dominio del sistema, dominio actual.
	 *
	 * @return
	 */
	private String getDominioSistema() {
		String dominio = "";
		try {
			InetAddress address = InetAddress.getLocalHost();
			dominio = address.getCanonicalHostName();
			if (dominio.contains(".")) {
				dominio = Utils.obtenerSegundaCad(dominio, ".");
			} else {
				dominio = System.getenv("USERDOMAIN");
			}
		} catch (UnknownHostException e) {
			logger.error("Error al intentar obtener el dominio: " + e.getMessage());
			logger.trace(e.getMessage());
		}
		return dominio;
	}

	/**
	 * Obtiene el nombre del host del sistema
	 *
	 * @return
	 */
	protected String getHostName() {
		String hostName = "";
		try {
			InetAddress address = InetAddress.getLocalHost();
			hostName = address.getCanonicalHostName();
			if (hostName.contains(".")) {
				hostName = Utils.obtenerCadIzquierda(hostName, ".");
			} else {
				logger.error("Error al intentar obtener el host name");
			}
		} catch (UnknownHostException e) {
			logger.error("Error al intentar obtener el host name: " + e.getMessage());
		}
		return hostName;
	}

	public char getCaracterBarra() {
		return '\\';
	}

	public String resuelveVariablesDeSistema(String mensaje) {
		Pattern pattern = Pattern.compile("%(\\w+)%");
		return resuelveVariablesDeSistema(mensaje, pattern);
	}

	public String escapaSaltosDeLinea(String cadena) {

		return StringEscapeUtils.escapeXml(cadena).replaceAll("" + (char) 13 + (char) 10, "#x0a;");
	}
}

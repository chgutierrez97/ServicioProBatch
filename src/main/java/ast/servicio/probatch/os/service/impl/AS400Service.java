package ast.servicio.probatch.os.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.Permission;
import com.ibm.as400.access.QSYSPermission;
import com.ibm.as400.access.RootPermission;
import com.ibm.as400.access.User;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.domain.UsuarioPermiso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.message.MensajeError;
import ast.servicio.probatch.os.service.OsService;
import ast.servicio.probatch.os.service.impl.as400.AS400Process;
import ast.servicio.probatch.service.ServicioAgente;

public class AS400Service extends OsService {

	private String user;
	private String pass;

	public AS400Service() {
	}

	@Override
	public boolean is_absolute_path(String x) {
		return false;
	}

	private boolean existeUsuario(String usuario) throws MensajeErrorException, AS400SecurityException, ErrorCompletingRequestException, InterruptedException,
			IOException, ObjectDoesNotExistException {
		logger.debug("Existe Usuario AS400");
		AS400 conection = new AS400();

		User usuarioAS400 = new User(conection, usuario);

		if (usuarioAS400.exists()) {
			return true;
		}
		return false;
	}

	@Override
	public int getPid(Process proceso) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, AS400SecurityException,
			ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException {
		AS400Process as400Process = (AS400Process) proceso;
		String number = as400Process.getCmd().getServerJob().getNumber();
		logger.debug("JOB NUMBER = " + number);
		return Integer.parseInt(number);

	}

	@Override
	public String[] getExecuteCommand(ParametrosProceso parametroP) throws Exception {

		this.user = parametroP.getUsuario().getNombre();
		this.pass = parametroP.getUsuario().getValor();

		ArrayList<String> paramsList = new ArrayList<String>();
		String[] cmdAsArray;
		paramsList.add(parametroP.getComando());
		String atributos = "";
		if (parametroP.getArgumentos() != null) {
			for (Atributo atributo : parametroP.getArgumentos()) {
				atributos += " " + atributo.getValor();
			}
		}

		paramsList.add(atributos);

		cmdAsArray = paramsList.toArray(new String[0]);

		for (String string : cmdAsArray) {
			logger.debug("PARAMETRO: " + string);
		}
		return cmdAsArray;
	}

	@Override
	public String getKillCommand(int pid) {
		return "";
	}

	@Override
	public boolean buscarUsuarioPermisos(String usuario, String permisos, String path, String dominio) throws MensajeErrorException {
		boolean resultado = false;
		logger.debug("Buscando permisos AS400");
		try {
			if (existeUsuario(usuario)) {

				List<UsuarioPermiso> usuarioPermisos = permisosUsuario(path);

				if (usuarioPermisos != null) {
					for (int i = 0; i < usuarioPermisos.size(); i++) {

						if (usuario.equalsIgnoreCase(((UsuarioPermiso) usuarioPermisos.get(i)).getUsuario())) {

							if (getEquivalenciasPermisos(permisos, ((UsuarioPermiso) usuarioPermisos.get(i)).getPermisos())) {
								return true;
							}
							return false;
						}
					}

					if ((((UsuarioPermiso) usuarioPermisos.get(0)).getListaAS() != null)
							&& (!((UsuarioPermiso) usuarioPermisos.get(0)).getListaAS().equalsIgnoreCase("*NONE"))) {
						String pathLista = "/QSYS.LIB/" + ((UsuarioPermiso) usuarioPermisos.get(0)).getListaAS() + ".AUTL";
						return buscarUsuarioPermisos(usuario, permisos, pathLista, dominio);
					}

					for (int i = 0; i < usuarioPermisos.size(); i++) {

						if (((UsuarioPermiso) usuarioPermisos.get(i)).getUsuario().contains("PUBLIC")) {

							if ((getEquivalenciasPermisos(permisos, ((UsuarioPermiso) usuarioPermisos.get(i)).getPermisos()))
									|| (((UsuarioPermiso) usuarioPermisos.get(i)).getPermisos().equalsIgnoreCase("*ALL"))) {
								return true;
							}
							return false;
						}

					}

				}

			}
		} catch (Exception e) {
			throw new MensajeErrorException(e.getMessage());
		}
		return resultado;
	}

	private List<UsuarioPermiso> permisosUsuario(String path) throws MensajeErrorException {
		List<UsuarioPermiso> usuarioPermisos = new ArrayList<UsuarioPermiso>();

		String aux = executeCommandOutputStringAS400(path);

		if (aux != null) {
			int puntoComaIndex = aux.indexOf(";");
			String listaAS = aux.substring(puntoComaIndex + 1);
			aux = aux.substring(0, puntoComaIndex);
			String[] usuarioPermisoVector = aux.split(" ");

			for (int i = 0; i < usuarioPermisoVector.length; i++) {
				String[] usuarioPermisosSplit = usuarioPermisoVector[i].split(":");

				UsuarioPermiso usuarioPermiso = new UsuarioPermiso();
				usuarioPermiso.setUsuario(usuarioPermisosSplit[0]);
				usuarioPermiso.setPermisos(usuarioPermisosSplit[1]);
				usuarioPermiso.setListaAS(listaAS);
				usuarioPermisos.add(usuarioPermiso);
			}
		}

		return usuarioPermisos;
	}

	public char getCaracterBarra() {
		return '/';
	}

	public String reemplazaExpRegArchivo(String nombre) {

		return nombre;

	}

	public String resuelveVariablesDeSistema(String mensaje) {
		return null;
	}

	public String escapaSaltosDeLinea(String cadena) {
		return StringEscapeUtils.escapeXml(cadena).replaceAll("\n", "#x0a;");
	}

	public String executeCommandOutputStringAS400(String path) throws MensajeErrorException {
		StringBuffer ret = new StringBuffer();
		AS400 sys = new AS400(ServicioAgente.cfg.getaS400Server(), ServicioAgente.cfg.getaS400User(), ServicioAgente.cfg.getaS400Pass());

		try {
			Permission misPermisos = new Permission(sys, path);

			if (2 == misPermisos.getType()) {
				Enumeration enumm = misPermisos.getUserPermissions();

				while (enumm.hasMoreElements()) {
					RootPermission tipoPermiso = (RootPermission) enumm.nextElement();

					ret.append(tipoPermiso.getUserID());
					ret.append(":");
					ret.append(tipoPermiso.getDataAuthority());
					ret.append(" ");
				}
				ret.append(";");
				ret.append(misPermisos.getAuthorizationList());
			} else if (1 == misPermisos.getType()) {
				Enumeration enumm = misPermisos.getUserPermissions();

				while (enumm.hasMoreElements()) {
					QSYSPermission tipoPermiso = (QSYSPermission) enumm.nextElement();

					ret.append(tipoPermiso.getUserID());
					ret.append(":");
					ret.append(tipoPermiso.getObjectAuthority());
					ret.append(" ");
				}
				ret.append(";");
				ret.append(misPermisos.getAuthorizationList());
			} else {
				throw new MensajeErrorException("Error, el path no tiene el formato esperado");
			}
		} catch (Exception e) {
			logger.error(new MensajeError("Error al solicitar permisos").getTramaString());
			throw new MensajeErrorException("Error al solicitar permisos");
		} finally {
			try {
				sys.disconnectAllServices();
			} catch (Exception e) {
				throw new MensajeErrorException("Error al cerrar la conexion con el servidor AS400");
			}

		}

		return ret.toString();
	}

	@Override
	public Process executeCommand(String[] cmd, String[] entorno, File dirEjecucion)
			throws IOException, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, ObjectDoesNotExistException {

		logger.debug("EXECUTECOMMAND AS400");
		AS400 sysConn = new AS400(ServicioAgente.cfg.getaS400Server(), ServicioAgente.cfg.getaS400User(), ServicioAgente.cfg.getaS400Pass());
		CommandCall cmdCall;
		try {
			sysConn.connectService(AS400.COMMAND);

			String command = "";
			StringBuilder sb = new StringBuilder();
			for (String element : cmd) {
				sb.append(element);
				sb.append(" ");
			}

			command = sb.toString().trim();

			logger.debug("EXECUTED COMMAND: " + command);

			cmdCall = new CommandCall(sysConn, command);
		} finally {
			sysConn.disconnectAllServices();
		}

		return new AS400Process(cmdCall);

	}

	public boolean getEquivalenciasPermisos(String permisosAVerificar, String permisosObtenidos) {
		boolean validacion = false;
		String permisosAVerificarMinuscula = permisosAVerificar.toLowerCase();
		String permisosObtenidosMinuscula = permisosObtenidos.toLowerCase();
		String permisosEquivalentes = convertirPermisosAS400(permisosAVerificar);

		String cadAuxiliar = "";

		if ((permisosObtenidosMinuscula.contains("none")) || (permisosObtenidosMinuscula.contains("exclude"))) {
			return false;
		}
		if (permisosEquivalentes.equalsIgnoreCase(permisosObtenidos)) {

			return true;
		}

		for (int i = 0; i < permisosAVerificar.length(); i++) {
			if ((permisosAVerificarMinuscula.charAt(i) == 'r') && (permisosObtenidosMinuscula.contains("r")) && (!cadAuxiliar.contains("r"))) {
				validacion = true;
				cadAuxiliar = cadAuxiliar + "r";
			} else if ((permisosAVerificarMinuscula.charAt(i) == 'x') && (permisosObtenidosMinuscula.contains("x")) && (!cadAuxiliar.contains("x"))) {
				validacion = true;
				cadAuxiliar = cadAuxiliar + "x";
			} else if ((permisosAVerificarMinuscula.charAt(i) == 'w') && (permisosObtenidosMinuscula.contains("w")) && (!cadAuxiliar.contains("w"))) {
				validacion = true;
				cadAuxiliar = cadAuxiliar + "w";
			} else {
				validacion = false;
				break;
			}
		}
		return validacion;
	}

	public String convertirPermisosAS400(String permisos) {
		if ((permisos.equalsIgnoreCase("r")) || (permisos.equalsIgnoreCase("x")) || (permisos.equalsIgnoreCase("rx")))
			return "*USE";
		if ((permisos.equalsIgnoreCase("w")) || (permisos.equalsIgnoreCase("rw")) || (permisos.equalsIgnoreCase("wx"))) {
			return "*CHANGE";
		}
		return "*ALL";
	}

	@Override
	public boolean dirOrFileExists(String path) {
		logger.debug("AS400 Service, validando directorios en as400");
		boolean existeArchivo = false;
		AS400 as400 = null;
		try {
			logger.debug("host= " + ServicioAgente.cfg.getaS400Server() + " user= " + ServicioAgente.cfg.getaS400User() + " pass= " + ServicioAgente.cfg.getaS400Pass());
			as400 = new AS400(ServicioAgente.cfg.getaS400Server(), ServicioAgente.cfg.getaS400User(), ServicioAgente.cfg.getaS400Pass());

			if (path.contains("QSYS.LIB") || path.contains("QDLS")) {

				existeArchivo = true;

			} else {
				IFSFile archivoAS400 = new IFSFile(as400, path);

				existeArchivo = archivoAS400.exists();

			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		} finally {
			as400.disconnectAllServices();
		}

		return existeArchivo;

	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

}

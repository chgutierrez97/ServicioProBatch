package ast.servicio.probatch.os.service.impl;

import ast.servicio.probatch.domain.UsuarioPermiso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.os.service.impl.WindowsService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclFileAttributeView;
import java.util.*;

public class AclWindowsService extends WindowsService {
	@Override
	protected List<UsuarioPermiso> getPermisosUsuarioJava(String path) throws MensajeErrorException {
		try {
			logger.debug("Obteniendo los permisos de usuario via JAVA");
			path = removeLastBackslashFromPath(path);

			String hostName = getHostName();

			List<UsuarioPermiso> usuarioPermisos = new ArrayList<UsuarioPermiso>();

			File dir = new File(path);

			AclFileAttributeView aclFileAttributeView = Files.getFileAttributeView(dir.toPath(), AclFileAttributeView.class);

			for (AclEntry aclEntry : aclFileAttributeView.getAcl()) {

				String domainAndUsername = aclEntry.principal().getName();
				logger.debug("Domain and username = " + domainAndUsername);
				int indexOfSlash;
				String domain = "";
				if ((indexOfSlash = domainAndUsername.indexOf("\\")) != -1) {
					domain = domainAndUsername.substring(0, indexOfSlash);
				}
				// logger.debug("Domain = " + domain);
				// System.out.println("Domain = " + domain);
				domain = domain.equalsIgnoreCase("NT AUTHORITY") || domain.equalsIgnoreCase(hostName) ? "" : domain;
				// logger.debug("Domain after validation= " + domain);
				// System.out.println("Domain after validation= " + domain);
				String username = domainAndUsername.substring(indexOfSlash + 1);
				// logger.debug("Username = " + username);
				// System.out.println("Username = " + username);
				AclPermissions permissions = new AclPermissions(aclEntry.permissions());
				// logger.debug("Permissions = " + aclEntry.permissions());
				// System.out.println("Permissions = " +
				// aclEntry.permissions());
				String perm = permissions.parsePermissions();
				logger.debug(String.format("Permisos %s detectados para usuario %s de dominio %s", perm, username, domain));
				// System.out.println(String.format("Permisos %s detectados para
				// usuario %s de dominio %s", perm, username, domain));
				usuarioPermisos.add(new UsuarioPermiso(username, perm, domain));
				// logger.debug("Usuario Permisos = " + usuarioPermisos);
				// System.out.println("Usuario Permisos = " + usuarioPermisos);
			}
			
			return usuarioPermisos;
		} catch (IOException e) {
			logger.error("ERROR IOException");
			throw new MensajeErrorException("Error al ejecutar el comando de validacion de permisos.");
		}
	}

	public static class AclPermissions {
		private Set<AclEntryPermission> permissions;

		public AclPermissions(Set<AclEntryPermission> permissions) {
			this.permissions = permissions;
		}

		public String parsePermissions() {
			String permissions = "";
			if (canRead()) {
				permissions += "r";
			}
			if (canWrite()) {
				permissions += "w";
			}
			if (canExecute()) {
				permissions += "x";
			}
			if (permissions == "") {
				permissions += "n";
			}

			return permissions;
		}

		private boolean canRead() {
			return permissions.contains(AclEntryPermission.READ_DATA);
		}

		private boolean canWrite() {
			return permissions.contains(AclEntryPermission.WRITE_DATA);
		}

		private boolean canExecute() {
			return permissions.contains(AclEntryPermission.EXECUTE);
		}

		@Override
		public String toString() {
			return "AclPermissions [permissions=" + permissions.toString() + "]";
		}
	}

}

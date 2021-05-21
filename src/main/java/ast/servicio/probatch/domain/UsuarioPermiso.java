package ast.servicio.probatch.domain;

public class UsuarioPermiso {
	// ultimo
	String usuario;
	String permisos;
	String listaAS;
	String dominio;

	public String getDominio() {
		return dominio;
	}

	public void setDominio(String dominio) {
		this.dominio = dominio;
	}

	public String getListaAS() {
		return listaAS;
	}

	public void setListaAS(String listaAS) {
		this.listaAS = listaAS;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getPermisos() {
		return permisos;
	}

	public void setPermisos(String permisos) {
		this.permisos = permisos;
	}

	public UsuarioPermiso() {

	}

	public UsuarioPermiso(String usuario, String permisos, String dominio) {
		this.usuario = usuario;
		this.permisos = permisos;
		this.dominio = dominio;
	}

	public UsuarioPermiso(String usuario, String dominio) {
		this.usuario = usuario;
		this.dominio = dominio;
	}

	@Override
	public String toString() {
		return "UsuarioPermiso [usuario=" + usuario + ", permisos=" + permisos + ", listaAS=" + listaAS + ", dominio=" + dominio + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dominio == null) ? 0 : dominio.hashCode());
		result = prime * result + ((listaAS == null) ? 0 : listaAS.hashCode());
		result = prime * result + ((permisos == null) ? 0 : permisos.hashCode());
		result = prime * result + ((usuario == null) ? 0 : usuario.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UsuarioPermiso other = (UsuarioPermiso) obj;
		if (dominio == null) {
			if (other.dominio != null)
				return false;
		} else if (!dominio.equalsIgnoreCase(other.dominio))
			return false;
		if (usuario == null) {
			if (other.usuario != null)
				return false;
		} else if (!usuario.equals(other.usuario))
			return false;
		return true;
	}
	
	

}

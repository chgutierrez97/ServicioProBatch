package ast.servicio.probatch.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.util.Utils;

public class Atributo implements Cloneable {

	private String tipo;
	private String nombre;
	private String valor;
	private String valorMostrar;
	private String controlar_todos;
	private String leer;
	private String adjuntar_resultado;
	Logger logger = LoggerFactory.getLogger(Atributo.class);

	public Atributo() {
		tipo = "";
		nombre = "";
		valor = "";
		valorMostrar = "";
		controlar_todos = "";
		leer = "";
		adjuntar_resultado = "";
	}

	public Atributo(String name, String value) {
		tipo = "";
		nombre = name;
		valor = value;
		valorMostrar = value;
		controlar_todos = "";
	}

	public Atributo(String name, String value, String tipo) {
		this(name, value);
		this.tipo = tipo;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		valor = valor.replace("%d", Utils.seccionaFecha("dd"));
		valor = valor.replace("%m", Utils.seccionaFecha("MM"));
		valor = valor.replace("%y", Utils.seccionaFecha("yy"));
		valor = valor.replace("%Y", Utils.seccionaFecha("yyyy"));
		this.valor = valor;
	}

	public String getValorMostrar() {
		return valorMostrar;
	}

	public void setValorMostrar(String valorMostrar) {
		this.valorMostrar = valorMostrar;
	}

	public String getControlar_todos() {
		return controlar_todos;
	}

	public void setControlar_todos(String controlarTodos) {
		controlar_todos = controlarTodos;
	}

	public String getLeer() {
		return leer;
	}

	public void setLeer(String leer) {
		this.leer = leer;
	}

	public String getAdjuntar_resultado() {
		return adjuntar_resultado;
	}

	public void setAdjuntar_resultado(String adjuntarResultado) {
		adjuntar_resultado = adjuntarResultado;
	}

	public Object clone() {
		Object object = null;
		try {
			object = super.clone();
		} catch (CloneNotSupportedException e) {

		}
		return object;

	}

	@Override public String toString() {
		return "Atributo{" +
				"tipo='" + tipo + '\'' +
				", nombre='" + nombre + '\'' +
				", valor='" + valor + '\'' +
				", controlar_todos='" + controlar_todos + '\'' +
				", leer='" + leer + '\'' +
				", adjuntar_resultado='" + adjuntar_resultado + '\'' +
				'}';
	}
}

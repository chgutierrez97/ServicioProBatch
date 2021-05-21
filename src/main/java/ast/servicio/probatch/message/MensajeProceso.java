package ast.servicio.probatch.message;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.os.service.OsService;
import ast.servicio.probatch.os.service.OsServiceFactory;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.threads.EjecutarProceso;
import ast.servicio.probatch.util.Utils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class MensajeProceso extends Mensaje {
	public static Logger logger = LoggerFactory.getLogger(MensajeProceso.class);
	public static boolean terminarThreadsLocal;
	public static ParametrosProceso parametroProcesoOriginal;

	public MensajeProceso(String mensajeEntrada) {
		super(mensajeEntrada);
	}

	@Override
	public Mensaje procesarMensaje(OutputStream osSalida) throws MensajeErrorException {
		if (processExist()) {
			return MessageFactory.crearMensajeError("error", "El proceso no puede ejecutarse por que ya hay un proceso corriendo con ese mismo ID");
		}
		Mensaje respuesta = null;
		ParametrosProceso parametrosProceso = XmlToObject(this.getTramaXml());
		respuesta = validaciones(parametrosProceso);

		if (respuesta == null) {
			return ejecutarProceso(osSalida, parametrosProceso);
		}
		return respuesta;
	}

	/**
	 * Valida si el proceso esta corriendo, en caso de ser asi en el array de
	 * estados el proceso tendra estado null. En caso de finalizar tendra un
	 * valor int.
	 *
	 * @return
	 */
	private boolean processExist() {
		List<EstadoProceso> listaEstado = ServicioAgente.getEstadoMensajes();
		if (listaEstado.isEmpty()) {
			return false;
		}
		String id = obtenerId(this.getTramaString());
		synchronized (listaEstado) {
			for (Iterator<EstadoProceso> iterator = listaEstado.iterator(); iterator.hasNext();) {
				EstadoProceso estadoProceso = iterator.next();
				if (estadoProceso.getId().equals(id) && estadoProceso.getEstado() == null) {
					return true;
				}
			}
		}
		return false;
	}

	private String obtenerId(String cad) {
		String conseguirId = cad.substring(cad.indexOf("id"));
		conseguirId = conseguirId.substring(conseguirId.indexOf('"') + 1, conseguirId.indexOf('"', conseguirId.indexOf('"') + 1));
		return conseguirId;
	}

	private Mensaje ejecutarProceso(OutputStream osSalida, ParametrosProceso parametroP) {
		try {

			String[] cmd = OsServiceFactory.getOsService().getExecuteCommand(parametroP);

			String strDirChdir = parametroP.getChdir();

			if (strDirChdir == null) {
				throw new MensajeErrorException("error", "El parametro <chdir/> no puede ser nulo!");
			}

			if (!Utils.validarExistenciaArchivo(strDirChdir)) {
				Mensaje mensaje = MessageFactory.crearMensajeError("<error id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\""
						+ parametroP.getTs() + "\">El directorio no existe: " + StringEscapeUtils.escapeXml(strDirChdir) + "</error>");
				return mensaje;

			}

			if (parametroP.getCategoria().equals("")) {
				Mensaje mensaje = MessageFactory.crearMensajeError("<error id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\""
						+ parametroP.getTs() + "\">Debe especificarse 'categoria'</error>");
				return mensaje;
			}

			File dirChdir = new File(strDirChdir);
			File dirCategoria = new File(ServicioAgente.cfg.getWrkdir() + "/" + parametroP.getCategoria());

			if (!dirCategoria.exists() || !dirCategoria.canWrite()) {
				Mensaje mensaje = MessageFactory.crearMensajeError("<error id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\""
						+ parametroP.getTs() + "\"> La carpeta \"" + parametroP.getCategoria() + "\" no existe o no tiene permiso de escritura " + "</error>");
				return mensaje;
			}

			Process process = null;
			try {
				/* se ejecuta el proceso */
				logger.debug("MensajeProceso::ejecutandoProceso");
				process = executeScriptToSO(parametroP, cmd, dirChdir);
				OsService osService = OsServiceFactory.getOsService();
				osService.idParametroProceso = parametroP.getId();
				/* se obtiene el pid del proceso lanzado */
				int pid = osService.getPid(process);

				logger.debug("MensajeProceso::ejecutarProceso::pid obtenido=" + pid);
				parametroP.setPid(pid);

			} catch (Exception e) {
				logger.error("Error al ejecutar proceso", e);
				Mensaje mensaje = MessageFactory
						.crearMensajeError("<error id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + parametroP.getTs()
								+ "\">La ruta al programa debe ser una especificacion absoluta o ser un comando predefinido: " + cmd[0] + "</error>");

				return mensaje;
			}
			// Revisar Estados //
			actualizarEstadoProceso(parametroP, null);
			// EjecutarProceso ejecutarProceso =
			// OsServiceFactory.getOsService().ejecutarProceso(process,
			// osSalida, parametroP);
			EjecutarProceso ejecutarProceso = new EjecutarProceso(process, osSalida, parametroP);
			ejecutarProceso.start();
			terminarThreadsLocal = true;

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			logger.error("Error al ejecutar el proceso", e);
			Mensaje mensaje = MessageFactory.crearMensajeError("<error id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\""
					+ parametroP.getTs() + "\">Error al ejecutar el proceso</error>");
			return mensaje;
		}

		return MessageFactory.crearMensajeRespuesta("<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\""
				+ parametroP.getTs() + "\" pidpadre=\"" + parametroP.getPid() + "\"><inicio/></transicion>");

	}

	/**
	 * Ejecuta el scipt segun el SO
	 *
	 * @param parametroP
	 * @param cmd
	 * @param dirChdir
	 * @return
	 * @throws IOException
	 */
	private Process executeScriptToSO(ParametrosProceso parametroP, String[] cmd, File dirChdir) throws Exception {
		Process process = OsServiceFactory.getOsService().executeCommand(cmd, parametroP.getVarEntornoArray(), dirChdir);

		return process;
	}

	/**
	 * Busca el estado, si lo encuentra lo pisa, si no crea una entrada nueva
	 * para el estado del proceso actual.
	 *
	 * @param parametroP
	 * @param estado
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public static void actualizarEstadoProceso(ParametrosProceso parametroP, Integer estado) {
		logger.debug("Estableciendo estado {} a proceso {}", estado, parametroP.getId());

		// logger.debug("actualizarEstadoProceso::actualizando estados
		// con:parametroP=" + parametroP.toString() + ";estado=" + estado);
		// logger.debug("MensajeProceso solicita lista estado mensajes...");

		List<EstadoProceso> listaProcesos = ServicioAgente.getEstadoMensajes();
		synchronized (listaProcesos) {
			EstadoProceso procesoActual = buscarEstadoProceso(listaProcesos, parametroP.getId());
			if (procesoActual != null) {
				procesoActual.setEstado(estado);
				procesoActual.setNombre(parametroP.getNombre());
				procesoActual.setPid(parametroP.getPid());
				procesoActual.setTs(parametroP.getTs());
				procesoActual.setDump(false);

			} else {
				listaProcesos.add(new EstadoProceso(parametroP.getId(), parametroP.getNombre(), parametroP.getTs(), parametroP.getPid(), null));
			}
		}
	}

	public static EstadoProceso buscarEstadoProceso(List<EstadoProceso> listaProcesos, String id) {
		for (EstadoProceso estadoProceso : listaProcesos) {
			if (estadoProceso.getId().equals(id)) {
				return estadoProceso;
			}

		}
		return null;
	}

	private Mensaje validaciones(ParametrosProceso parametrosProceso) throws MensajeErrorException {
		boolean existeUsuario = false;
		ArrayList<String> listaUsuarios = ServicioAgente.cfg.getUsuarios();
		Atributo usuario = parametrosProceso.getUsuario();
		File categoria = new File(ServicioAgente.cfg.getWrkdir() + "/" + parametrosProceso.getCategoria());

		if (!categoria.isDirectory()) {
			return MessageFactory.crearMensajeError("error", parametrosProceso.getId(), parametrosProceso.getNombre(),
					"La carpeta: " + "'" + parametrosProceso.getCategoria() + "'" + " no existe", null);
		}
		for (String usuarioConfig : listaUsuarios) {
			if (usuario.getNombre().equalsIgnoreCase(usuarioConfig)) {
				existeUsuario = true;
				break;
			}
		}

		if (parametrosProceso.getUsuario().getNombre().equals("")) {
			return MessageFactory.crearMensajeError("error", parametrosProceso.getId(), parametrosProceso.getNombre(), "Debe especificarse 'usuario'", null);
		}

		if (!existeUsuario) {
			return MessageFactory.crearMensajeError("error", parametrosProceso.getId(), parametrosProceso.getNombre(),
					"Usuario invalido: " + parametrosProceso.getUsuario().getNombre(), null);
		}

		if (validacionInterfaces(parametrosProceso.getInterfaces(), "entrada", parametrosProceso.getPatrones(), parametrosProceso.getId(),
				parametrosProceso.getNombre()) != null) {
			return MessageFactory.crearMensajeError("error", parametrosProceso.getId(), parametrosProceso.getNombre(),
					"Faltan archivos de entrada: " + validacionInterfaces(parametrosProceso.getInterfaces(), "entrada", parametrosProceso.getPatrones(),
							parametrosProceso.getId(), parametrosProceso.getNombre()),
					null);
		}

		return null;
	}

	public String validacionInterfaces(Collection<Atributo> interfaces, String tipo, Collection<Atributo> patrones, String id, String nombre)
			throws MensajeErrorException {

		logger.debug("VALIDANDO INTERFACES {} DE TIPO {}", interfaces, tipo);
		logger.debug("PATRONES: {}", patrones);

		String interfaz = null;
		String noEcontrados = null;
		String controlarTodos = null;

		for (Atributo atributo : interfaces) {
			if (tipo.equalsIgnoreCase(atributo.getNombre())) {
				interfaz = atributo.getValor();
				controlarTodos = atributo.getControlar_todos();
			}
		}
		if (interfaz != null) {
			noEcontrados = Utils.archivosNoEncontrados(interfaz);
			validaPatron(patrones, noEcontrados, id, nombre);
			if (!noEcontrados.equals("")) {

				if (controlarTodos.equals("1")) {
					return noEcontrados;

				} else if (controlarTodos.equals("0") && noEcontrados.equals(interfaz)) {
					return noEcontrados;

				}

			}
		}
		return null;

	}

	public ParametrosProceso XmlToObject(Document input) throws MensajeErrorException {

		ParametrosProceso paramProc = new ParametrosProceso();
		try {
			Element raiz = input.getDocumentElement();

			NodeList hijosNodoProceso = raiz.getChildNodes();

			if (Utils.esNumerico(raiz.getAttribute("id"))) {
				paramProc.setId(raiz.getAttribute("id"));
			} else {
				throw new MensajeErrorException("error", raiz.getAttribute("id"), raiz.getAttribute("nombre"), "Id no numerica: " + raiz.getAttribute("id"));
			}

			String key = ServicioAgente.cfg.getKey();

			paramProc.setTs(getTs());

			Collection<Atributo> argumentos = new ArrayList<Atributo>();
			Collection<Atributo> argumentos_originales = new ArrayList<Atributo>();
			Collection<Atributo> entorno = new ArrayList<Atributo>();
			Collection<Atributo> interfaces = new ArrayList<Atributo>();
			Collection<Atributo> patrones = new ArrayList<Atributo>();

			paramProc.setNombre(raiz.getAttribute("nombre"));
			paramProc.setCategoria(raiz.getAttribute("categoria"));
			paramProc.setClase(raiz.getAttribute("clase"));

			int i = 0;
			for (i = 0; i < hijosNodoProceso.getLength(); i++) {
				Node nodo = hijosNodoProceso.item(i);

				if (nodo.getNodeType() == Node.TEXT_NODE) {
					continue;
				}
				String nombreNodo = nodo.getNodeName().trim();
				if (nombreNodo.equals("chdir")) {
					paramProc.setChdir(nodo.getTextContent());
				}

				if (nombreNodo.equals("comando")) {
					if (nodo.getTextContent() == null || nodo.getTextContent().equals("")) {
						throw new MensajeErrorException("El campo comando es obligatorio");
					}
					paramProc.setComando(ServicioAgente.cfg.getComandos(nodo.getTextContent()));
				}

				if (nombreNodo.equals("arg")) {
					Atributo argAttr = new Atributo("", nodo.getTextContent());
					Atributo arg_org = (Atributo) argAttr.clone();
					if (nodo.getAttributes().getNamedItem("tipo") != null) {
						argAttr.setTipo(nodo.getAttributes().getNamedItem("tipo").getTextContent());
						if (argAttr.getTipo().equals("oculto")) {
							argAttr.setValor(new String(Utils.xorstr(key, paramProc.getId(), nodo.getTextContent())).trim());
							argAttr.setValorMostrar(new String(Utils.xorstr(key, paramProc.getId(), nodo.getTextContent())));
						}
					}
					argumentos_originales.add(arg_org);
					argumentos.add(argAttr);
				}

				if (nombreNodo.equals("usuario")) {
					String s_clave = nodo.getAttributes().getNamedItem("clave").getTextContent();
					Atributo usrAttr = new Atributo(nodo.getTextContent(), new String(Utils.xorstr(key, paramProc.getId(), s_clave)));

					paramProc.setUsuario(usrAttr);
				}

				if (nombreNodo.equals("entorno")) {
					NodeList hijosEntorno = nodo.getChildNodes();

					for (int a = 0; a < hijosEntorno.getLength(); a++) {
						Node hijoEntorno = hijosEntorno.item(a);

						if (hijoEntorno.getNodeName().trim().equals("var")) {
							Atributo varAttr = new Atributo(hijoEntorno.getAttributes().getNamedItem("nombre").getTextContent(), hijoEntorno.getTextContent());

							if (hijoEntorno.getAttributes().getNamedItem("tipo") != null) {
								varAttr.setTipo(hijoEntorno.getAttributes().getNamedItem("tipo").getTextContent());
								if (varAttr.getTipo().equals("oculto")) {
									varAttr.setValor(new String(Utils.xorstr(key, paramProc.getId(), hijoEntorno.getTextContent())));
								}
								varAttr.setValorMostrar(new String(Utils.xorstr(key, paramProc.getId(), hijoEntorno.getTextContent())));
							}
							entorno.add(varAttr);
						}
					} 
				}

				if (nombreNodo.equals("patron")) {
					NodeList hijosPatron = nodo.getChildNodes();

					for (int a = 0; a < hijosPatron.getLength(); a++) {

						Node hijoPatron = hijosPatron.item(a);

						if (hijoPatron.getNodeType() == Node.TEXT_NODE) {
							continue;
						}
						Node nodoTipo = hijoPatron.getAttributes().getNamedItem("tipo");
						String tipo = nodoTipo == null ? "glob" : nodoTipo.getTextContent();

						Atributo patronAttr = new Atributo(hijoPatron.getNodeName().trim(), hijoPatron.getTextContent(), tipo);

						if (tipo.equals("oculto")) {
							patronAttr.setValor(new String(Utils.xorstr(key, paramProc.getId(), nodo.getTextContent())));
						}

						patrones.add(patronAttr);
					}
				}

				if (nombreNodo.equals("resultado")) {
					Atributo resAttr = new Atributo("resultado", nodo.getTextContent());
					paramProc.setResultado(resAttr);
				}

				if (nombreNodo.equals("interfaces")) {
					NodeList hijosInterfaces = nodo.getChildNodes();

					for (int a = 0; a < hijosInterfaces.getLength(); a++) {
						Node hijoInterfaces = hijosInterfaces.item(a);
						if (hijoInterfaces.getNodeType() == Node.ELEMENT_NODE) {
							Atributo interfAttr = new Atributo(hijoInterfaces.getNodeName().trim(), hijoInterfaces.getTextContent());

							if (hijoInterfaces.getAttributes().getNamedItem("controlar_todos") != null) {
								interfAttr.setControlar_todos(hijoInterfaces.getAttributes().getNamedItem("controlar_todos").getTextContent());
							}
							interfaces.add(interfAttr);
						}
					}
				}
			}

			if (paramProc.getComando() == null || paramProc.getComando().equals("")) {
				throw new MensajeErrorException("El campo comando es obligatorio");
			} else if (paramProc.getNombre() == null || paramProc.getNombre().equals("")) {
				throw new MensajeErrorException("El campo nombre es obligatorio");
			}
			paramProc.setEntorno(entorno);
			paramProc.setInterfaces(interfaces);
			paramProc.setPatrones(patrones);
			paramProc.setArgumentos(argumentos);

			parametroProcesoOriginal = (ParametrosProceso) paramProc.clone();
			parametroProcesoOriginal.setArgumentos(argumentos_originales);

		} catch (MensajeErrorException m) {
			throw m;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error en la sintaxis del mensaje. Exception: " + e.getMessage());
			logger.error("Error en la sintaxis del mensaje. Exception: " + e);
			throw new MensajeErrorException("Error en la sintaxis del mensaje.");
		}

		return paramProc;
	}

	public String validaPatron(Collection<Atributo> patrones, String salidaProceso, String id, String nombre) throws MensajeErrorException {
		String ignore_re = ServicioAgente.cfg.getIgnore_re();

		if (!patrones.isEmpty()) {
			for (Iterator<Atributo> iterator = patrones.iterator(); iterator.hasNext();) {
				Atributo atributo = (Atributo) iterator.next();
				if (ParametrosProceso.FATAL.equals(atributo.getNombre()) && Utils.validaExpresionesRegulares(salidaProceso, atributo.getValor())) {
					throw new MensajeErrorException("fatal", id, nombre, salidaProceso);
				}
			}

			for (Iterator<Atributo> iterator = patrones.iterator(); iterator.hasNext();) {
				Atributo atributo = (Atributo) iterator.next();

				if (ParametrosProceso.IGNORAR.equals(atributo.getNombre()) && atributo.getTipo().equals("glob")
						&& Utils.validaExpresionesRegulares(salidaProceso, atributo.getValor())) {
					return salidaProceso;
				} else if (ParametrosProceso.IGNORAR.equals(atributo.getNombre()) && atributo.getTipo().equals("re")
						&& (Utils.validaExpresionesRegulares(salidaProceso, atributo.getValor())
								|| Utils.validaExpresionesRegulares(salidaProceso, ignore_re))) {
					return salidaProceso;
				}
				return null;
			}

		}
		return null;
	}

}

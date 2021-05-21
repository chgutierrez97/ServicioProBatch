package ast.servicio.probatch.monitoring;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.util.TraductorStatus;

public abstract class MonitorProcess {

	Logger logger = LoggerFactory.getLogger(MonitorProcess.class);
	private static SortedSet<String> children = new TreeSet<String>();

	private String commandSearchPid;

	private String commandSearchStatus;

	private final String EMPTY = " ";

	public String getCommandSearchPid() {
		return commandSearchPid;
	}

	public void setCommandSearch(String commandSearchPid) {
		this.commandSearchPid = commandSearchPid;
	}

	public String getCommandSearchStatus() {
		return commandSearchStatus;
	}

	public void setCommandSearchStatus(String commandSearchStatus) {
		this.commandSearchStatus = commandSearchStatus;
	}

	/**
	 * Busca de formar recursiva , todos los pid hijos de un pid determinado y
	 * los almacena en una lista estatica..
	 * 
	 * @param currPid
	 *            (PID del proceso padre)
	 * @param command
	 *            (comando para la busqueda de procesos hijos) varia segun el
	 *            sistema operativo
	 * @throws Exception
	 */
	public void getChildrenPid(int currPid) throws Exception {

		String[] cmd = { "/bin/sh", "-c", getCommandSearchPid() + currPid };

		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(cmd);
				

		int last_pid = 0;
		String newPid = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

		while ((newPid = br.readLine()) != null) {
			
			
			newPid = newPid.trim();
			newPid = newPid.substring(0, newPid.indexOf(EMPTY));
			if (currPid > Integer.valueOf(newPid)) {
				children.add(newPid);
				return;
			}

			if (Integer.toString(currPid).contentEquals(newPid)) {
				continue;
			} else {
				logger.debug("found child pid: " + newPid);
				last_pid = Integer.valueOf(newPid);
				children.add(newPid);
			}
		}

		if (last_pid == 0) {
			logger.debug("no children pids found...");
			return;
		} else {
			getChildrenPid(last_pid);
		}

	}

	/**
	 * Obtiene el estado de cada pid hijo almacenados en una lista, y devuelve
	 * otra con los datos actualizados
	 * 
	 * @param command
	 *            (comando a ejecutar busqueda de estados, varia segun el
	 *            sistema operativo)
	 * @param currPid
	 *            (PID del proceso padre)
	 * @return statusList(devuelve una lista con todos los estados de cada
	 *         subproceso)
	 * @throws Exception
	 */
	public List<String> getListStatus(int currPid) throws Exception {

		getChildrenPid(currPid);
		logger.debug("children pids: " + children.toString());
		List<String> statusList = new ArrayList<String>();
		SubProcess subproceso = null;
		String mensajeStatus = null;

		Runtime runtime = Runtime.getRuntime();
		for (String child : children) {

			subproceso = new SubProcess();
			try {
				String[] values = null;
				String[] commandAsArray = { "/bin/sh", "-c", getCommandSearchStatus() + child };

				Process process = runtime.exec(commandAsArray);
				BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String row_status = null;

				while ((row_status = buffer.readLine()) != null) {

					/**
					 * la primera linea es el titulo de los datos en mayusculas,
					 * o un espacio en blanco, no es requerido
					 * 
					 */

					if (row_status.contains("PID") || row_status.equals("")) {
						continue;
					}
					/**
					 * Obtengo un array con todos los datos del proceso, separo
					 * los datos por espacios con un split.
					 */
					row_status = row_status.trim();
					values = row_status.split("\\s+");
					break;
				}

				/**
				 * Guardo los datos en un objeto llamado "Subproceso" para poder
				 * trabajar mejor
				 */
				subproceso.setPid(values[0]);
				subproceso.setUser(values[1]);

				/**
				 * los estados estan representados por una letra mayuscula
				 * ejemplo R = Running, debo traducir la letra al estado que
				 * esta referenciando
				 * 
				 */
				String letra_status = values[2].substring(0, 1);
				String status_traducido = TraductorStatus.traduce(letra_status.trim());

				subproceso.setState(status_traducido);
				subproceso.setTime(values[3]);
				StringBuilder nombre = new StringBuilder();
				for (int j = 4; j < values.length; j++) {
					nombre.append(values[j]);
				}
				subproceso.setName(nombre.toString());

				/**
				 * genero la trama para cada proceso
				 */
				mensajeStatus = "<proceso nombre=\"" + subproceso.getName() + "\" usuario=\"" + subproceso.getUser() + "\" numero=\"" + subproceso.getPid()
						+ "\"  estado=\"" + subproceso.getState() + "\" hora-inicio=\"" + subproceso.getTime() + "\"/>";

				/**
				 * guardo la trama en una lista
				 * 
				 */

				statusList.add(mensajeStatus);

			} catch (Exception e) {
				logger.debug(e.getMessage());
			}
		}
		/**
		 * devuelvo la lista con todos los estados de los procesos encontrados
		 * 
		 */
		return statusList;
	}

	public List<String> getSubprocessesState(int currPid) throws Exception {
		return null;
	}

}

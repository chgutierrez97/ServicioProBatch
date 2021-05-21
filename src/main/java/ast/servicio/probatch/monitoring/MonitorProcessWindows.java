package ast.servicio.probatch.monitoring;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorProcessWindows extends MonitorProcess {

	Logger logger = LoggerFactory.getLogger(MonitorProcessWindows.class);

	private SortedSet<String> childrenpid = new TreeSet<String>();

	/**
	 * metodo que me permite obtener de forma recursiva todos los procesos hijos
	 * de un proceso padre dado
	 * 
	 * @param int currPid
	 * 
	 */

	@Override
	public void getChildrenPid(int currPid) throws Exception {

		String[] command = { "cmd.exe", "/C", "wmic process where (parentprocessid= " + currPid + " ) get processid /VALUE" };
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(command);

		String newLine = null;
		String newPid = null;
		BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
		while ((newLine = buffer.readLine()) != null) {

			if (newLine.contains("ProcessId")) {
				newPid = newLine.substring(newLine.indexOf("=") + 1, newLine.length());
				logger.debug("child pid found :" + newPid);
				childrenpid.add(newPid.trim());
				break;
			} else {
				logger.debug("no children pids found");
				return;
			}
		}

		if (newPid != null) {
			int nextPid = Integer.valueOf(newPid);
			getChildrenPid(nextPid);
		} else {
			logger.debug("no more children was found");
			return;
		}
	}

	public List<String> getListStatus(int currPid) throws Exception {
		getChildrenPid(currPid);
		logger.debug("list of child " + childrenpid.toString());
		List<String> statusChild = new ArrayList<String>();
		SubProcess subproceso = null;
		/**
		 * recorro la lista para trabajar con cada pid encontrado
		 */
		for (String child : childrenpid) {

			subproceso = new SubProcess();
			String[] command = { "cmd.exe", "/C", "wmic process where (processid=" + child + ") get creationdate /VALUE" };
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(command);

			BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String newLine = null;
			String start_time = null;

			/**
			 * de esta ejecucion de cmd obtengo la fecha de creacion del proceso
			 * para guardarla junto con el pid
			 */

			while ((newLine = buffer.readLine()) != null) {

				if (newLine.contains("CreationDate")) {
					start_time = newLine.substring(newLine.indexOf("=") + 1, newLine.length());
					start_time = format(start_time);
					break;
				}
			}

			subproceso.setPid(child);
			subproceso.setTime(start_time);

			newLine = null;

			String[] newCommand = { "cmd.exe", "/C", "tasklist /v /fi \"PID eq " + child + "\"  /fo list" };
			process = runtime.exec(newCommand);

			/**
			 * recorro las filas que obtuve como resultado de este cmd, buscando
			 * nombre de proceso , estados , nombre de usuario
			 * 
			 */

			buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((newLine = buffer.readLine()) != null) {

				/**
				 * guardo el nombre del proceso
				 */
				if (newLine.contains("Nombre de imagen") || newLine.contains("Image Name")) {
					String name = extractParameter(newLine);
					subproceso.setName(name);
				}

				/**
				 * guardo el estado
				 */
				if (newLine.contains("Estado") || newLine.contains("Status")) {
					String state = extractParameter(newLine);
					subproceso.setState(state);
				}

				/**
				 * guardo el nombre de usuario
				 */
				if (newLine.contains("Nombre de usuario") || newLine.contains("User name")) {
					String user = extractParameter(newLine);
					subproceso.setUser(user);
				}

			}

			createMessageStatus(statusChild, subproceso);
		}

		return statusChild;

	}

	/**
	 * genero la trama para cada proceso y la guardo en una lista
	 * 
	 * @param statusChild
	 * @param subproceso
	 */
	private void createMessageStatus(List<String> statusChild, SubProcess subproceso) {
		String mensajeStatus;
		mensajeStatus = "<proceso nombre=\"" + subproceso.getName() + "\" usuario=\"" + subproceso.getUser() + "\" pid=\"" + subproceso.getPid()
				+ "\"  estado=\"" + subproceso.getState() + "\" hora-inicio=\"" + subproceso.getTime() + "\"/>";

		statusChild.add(mensajeStatus);
	}

	@Override
	public List<String> getSubprocessesState(int currPid) throws Exception {

		List<String> allStatus = getListStatus(currPid);
		return allStatus;

	}

	/**
	 * 
	 * 
	 * la fecha de creacion viene en este formato = 20160315111013.830879-180
	 * por eso debo parsearla a un formato legible
	 * 
	 * @param start_time
	 * @return
	 */
	private String format(String start_time) {
		String year = start_time.substring(0, 4);
		String month = start_time.substring(4, 6);
		String day = start_time.substring(6, 8);
		String hours = start_time.substring(8, 10);
		String minutes = start_time.substring(10, 12);
		String seconds = start_time.substring(12, 14);
		return start_time = day + "-" + month + "-" + year + " " + hours + ":" + minutes + ":" + seconds;
	}

	/**
	 * 
	 * los datos conseguidos vienen en formato clave:valor, lo que busco es
	 * obtener es el valor
	 * 
	 * @param line
	 * @return
	 */
	private String extractParameter(String line) {
		String[] mapa = line.split(":");
		String value = mapa[1];
		return value.trim();
	}

}

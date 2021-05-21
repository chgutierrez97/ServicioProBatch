package ast.servicio.probatch.monitoring;

import java.util.List;

/**
 * @author marcos.barroso
 *
 * Implementation Solaris
 */
public class MonitorProcessSolaris extends MonitorProcess {

	public List<String> getSubprocessesState(int currPid) throws Exception {

		String command_to_seach_children = "ps -efo pid,ppid | grep ";
		setCommandSearch(command_to_seach_children);
		String command_to_search_status = "ps -efo pid,user,s,stime,comm | grep ";
		setCommandSearchStatus(command_to_search_status);
		List<String> statusList = getListStatus(currPid);

		return statusList;

	}

}

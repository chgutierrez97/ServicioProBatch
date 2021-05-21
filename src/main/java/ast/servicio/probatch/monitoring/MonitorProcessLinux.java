package ast.servicio.probatch.monitoring;

import java.util.List;

/**
 * @author marcos.barroso
 * 
 * Implementation LINUX
 *
 */
public class MonitorProcessLinux extends MonitorProcess {

	public List<String> getSubprocessesState(int currPid) throws Exception {

		String command_to_seach_children = "ps -xao pid,ppid | grep ";
		setCommandSearch(command_to_seach_children);
		String command_to_search_status = "ps -xao pid,user,stat,stime,cmd  | grep ";
		setCommandSearchStatus(command_to_search_status);
		List<String> statusList = getListStatus(currPid);
		return statusList;
	}

}

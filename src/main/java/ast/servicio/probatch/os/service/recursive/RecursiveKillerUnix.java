package ast.servicio.probatch.os.service.recursive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecursiveKillerUnix implements RecursiveKiller {

	
	private Logger logger = LoggerFactory.getLogger(RecursiveKillerUnix.class);
	
	public RecursiveKillerUnix() {
		super();
	}

	public void initKiller(int mainPid) {
		try {
			__run(mainPid);
		} catch (IOException e) {
			logger.debug("error: " + e.toString());
		}

	}

	private void __run(int currPid) throws IOException {

		String[] cmd = { "/bin/sh", "-c", "ps -efo pid,ppid | grep " + currPid + " | cut -d\" \" -f2" };
		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(cmd);

		String line = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		List<Integer> childrenPids = new ArrayList<Integer>();

		while ((line = br.readLine()) != null) {
			line = line.trim();

			if (currPid > Integer.valueOf(line)) {
				break;
			}

			if (Integer.toString(currPid).contentEquals(line)) {
				continue;
			} else {
				// logger.debug("found child pid: " + line);
				childrenPids.add(Integer.parseInt(line));
			}
		}

		String killCmd = "kill -9 " + currPid;
		rt.exec(killCmd);

		if (childrenPids.isEmpty()) {
			// logger.debug("no children pids found...");
			return;
		} else {
			// logger.debug("children pids: " + childrenPids.toString());
			for (Integer nextPid : childrenPids) {
				__run(nextPid);
			}
		}
	}// __run

}

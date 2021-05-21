package ast.servicio.probatch.os.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProcessRecursiveKiller {
	private int mainPid;

	public ProcessRecursiveKiller(int mainPid) {
		super();
		this.mainPid = mainPid;
	}

	public void run() {
		System.out.println("RecursiveKiller start...");

		try {
			__run(mainPid);
		} catch (IOException e) {
			System.err.println("error: " + e.toString());
		}

		System.out.println("RecursiveKiller end...");
	}

	private void __run(int currPid) throws IOException {
		// ps xao pid,ppid | grep $PID | cut -f2 -d" "
		String[] cmd = { "/bin/sh", "-c", "ps xao pid,ppid | grep " + currPid + " | cut -f2 -d\" \"" };
		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(cmd);

		String line = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		List<Integer> childrenPids = new ArrayList<Integer>();

		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (Integer.toString(currPid).contentEquals(line)) {
				continue;
			} else {
				System.out.println("found child pid: " + line);
				childrenPids.add(Integer.parseInt(line));
			}
		}

		System.out.println("killing " + currPid);
		String killCmd = "kill -9 " + currPid;
		rt.exec(killCmd);

		if (childrenPids.isEmpty()) {
			System.out.println("no children pids found...");
			return;
		} else {
			System.out.println("children pids: " + childrenPids.toString());
			for (Integer nextPid : childrenPids) {
				__run(nextPid);
			}
		}
	}//__run

	public void testPipes() throws IOException {
		System.out.println("running testPipes...");

		//		/bin/bash -c 'ps aux | grep eternal'

		Runtime rt = Runtime.getRuntime();
		String[] cmd = { "/bin/sh", "-c", "ps xao pid,ppid | grep 2769 | cut -f2 -d\" \"" };
		Process proc = rt.exec(cmd);
		BufferedReader is = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String line = "";
		while ((line = is.readLine()) != null) {
			System.out.println(line);
		}

		System.out.println("end of testPipes");
	}

	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			System.out.println("call: RecursiveKiller pid");
			return;
		}
		int pid = Integer.parseInt(args[0].trim());
		new ProcessRecursiveKiller(pid).run();
	}
}

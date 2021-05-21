package ast.servicio.probatch.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import ast.servicio.probatch.os.service.impl.WindowsService;

public class GetWindowsPidTest {
	Runtime runtime = Runtime.getRuntime();
	String taskListCmd = "wmic process get processid,parentprocessid,executablepath";

	private void testRunDummyBat() throws InterruptedException, IOException, SecurityException, IllegalArgumentException, NoSuchFieldException,
			IllegalAccessException {
		int cmdSleepArg = 5;
		String s_cmd = "C:\\Users\\martin.zaragoza\\Desktop\\dummyBat.bat" + " " + cmdSleepArg;
		Process process = runtime.exec(s_cmd);

		InputStream processInputStream = process.getInputStream();
		final BufferedReader READER = new BufferedReader(new InputStreamReader(processInputStream));
		Runnable runnableReader = new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
						String line = READER.readLine();
						if (lineIsNullOrEmpty(line)) {
							continue;
						}
						log("read line:" + line);
					} catch (Exception e) {
					}
				}
			}

			private boolean lineIsNullOrEmpty(String line) {
				return line == null || line.contentEquals("");
			}
		};

		Thread readerThread = new Thread(runnableReader);
		WindowsService windowsService = new WindowsService();
		int pid = windowsService.getPid(process);
		log("started " + pid);
		readerThread.start();

		long seconds = 120;
		long mainThreadSleepTime = seconds * 1000;
		log("running for " + seconds + " seconds");
		Thread.sleep(mainThreadSleepTime);
		readerThread.interrupt();

		log("end...");
	}

	private void testGetChildrenPids() throws IOException, SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
			InterruptedException {
		log("testGetChildrenPids started");

		int cmdSleepArg = 5;
		String s_cmd = "C:\\Users\\martin.zaragoza\\Desktop\\dummyBat.bat" + " " + cmdSleepArg;
		Process process = runtime.exec(s_cmd);

		Thread.sleep(1000);

		WindowsService windowsService = new WindowsService();
		int pid = windowsService.getPid(process);
		log("started " + pid);

		// String cmd = taskListCmd + " | find " + "\"" + 4708 + "\"";
		String cmd = taskListCmd;
		Process tlProcess = runtime.exec(cmd);
		tlProcess.waitFor();
		InputStream inputStream = tlProcess.getInputStream();
		printProcessInputStream(inputStream);

		log("testGetChildrenPids ended");
	}

	private void testPrintTaskList() throws IOException, InterruptedException {
		printTaskList();
	}

	public static void main(String[] args) throws IOException, InterruptedException, SecurityException, IllegalArgumentException, NoSuchFieldException,
			IllegalAccessException {
		GetWindowsPidTest getWindowsPidTest = new GetWindowsPidTest();
		getWindowsPidTest.printTaskList();
	}// testGetWindowsPid

	private void printProcessInputStream(InputStream inputStream) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

		while (true) {
			String line = br.readLine();
			if (line == null) {
				break;
			}
			line = line.trim();
			if (line.contentEquals("")) {
				continue;
			}

			System.out.println(line);
		}
	}

	private synchronized void printTaskList() throws IOException, InterruptedException {
		log("Running tasklist...");

		String[] cmd = { "wmic process get processid,parentprocessid" };

		Process taskListProcess = runtime.exec("wmic process get processid,parentprocessid");
		long timeout = 2000;
		synchronized (taskListProcess) {
			taskListProcess.wait(timeout);
		}
		InputStream taskListInputStream = taskListProcess.getInputStream();

		printProcessInputStream(taskListInputStream);
	}

	private void log(String s) {
		System.out.println("GetWindowsPidTest::" + s);
	}

	public long getPid(Process proceso) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field f = proceso.getClass().getDeclaredField("handle");
		f.setAccessible(true);
		long handl = f.getLong(proceso);

		return handl;
	}
}

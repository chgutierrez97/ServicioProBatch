package ast.servicio.probatch.test.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

//import junit.framework.TestCase;

public class GetWindowsPidsTest /*extends TestCase */ {
	private static abstract class BufferedReaderLineIterator {
		public void iterate(BufferedReader bufferedReader) throws IOException {
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				__work(line);
			}
		}

		protected abstract void __work(String line);
	}
	
	private static abstract class ProcessRunner {
		public void run(){
			
		}
	}

	public void testGetWindowsChildrenPid() throws IOException, InterruptedException {
		log("testGetWindowsChildrenPid start");

		String command = "wmic process get processid,parentprocessid";
		Process process = Runtime.getRuntime().exec(command);

		long timeout = 1000;
		synchronized (process) {
			process.wait(timeout);
			InputStream inputStream = process.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

			BufferedReaderLineIterator bufferedReaderLineIterator = new BufferedReaderLineIterator() {
				protected void __work(String line) {
					line = line.trim();
					String ppid = "4708";
					if (line.startsWith(ppid)) {
						StringTokenizer st = new StringTokenizer(line, " ");
						st.nextToken();
						log("child of " + ppid + "=" + st.nextToken());
					}
				}
			};// BufferedReaderLineIterator

			bufferedReaderLineIterator.iterate(bufferedReader);
		}

		log("testGetWindowsChildrenPid end");
	}//testGetWindowsChildrenPid
	
	public void testGetWindowsParentPid() throws IOException{
		log("testGetWindowsParentPid start");
		
		String command = "wmic process get processid,parentprocessid";
		Process process = Runtime.getRuntime().exec(command);

		synchronized (process) {
			
		}
		
		log("testGetWindowsParentPid end");
	}

	public void testGetWindowsRunningPids() throws IOException, InterruptedException {
		log("start testGetWindowsRunningPids");

		String command = "tasklist /fi \"status eq running\"";
		Process process = Runtime.getRuntime().exec(command);

		synchronized (process) {
			long timeout = 1000;
			process.wait(timeout);

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReaderLineIterator bufferedReaderLineIterator = new BufferedReaderLineIterator() {
				protected void __work(String line) {
					line = line.trim();

					if (line.contentEquals("")) {
					} else {
						StringTokenizer st = new StringTokenizer(line, " ");
						st.nextToken();

						String pid = st.nextToken();
						try {
							Integer.parseInt(pid);
							log(pid);
						} catch (NumberFormatException e) {
						}
					}
				}// __work
			};// BufferedReaderLineIterator

			bufferedReaderLineIterator.iterate(bufferedReader);

			log("end testGetWindowsRunningPids");
		}
	}//testGetWindowsRunningPids

	private void log(String s) {
		System.out.println("RegexpTest::" + s);
	}
}

package ast.servicio.probatch.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ast.servicio.probatch.os.service.util.Kernel32;
import ast.servicio.probatch.os.service.util.W32API;

import com.sun.jna.Pointer;

public class RunCmdTest {
	public static void main(String[] args) throws IOException {
		testRunHardcodedCmd();
	}

	/*
	 * ERROR CreateProcess failed 1314 implica que no se tienen los privilegios
	 * necesarios!
	 */
	private static void testRunHardcodedCmd() {
		String[] cmdArray = { "./runAsUser/runAsUser.exe", "c:\\procesos", "martin.zaragoza@ACCUSYSARGBSAS", "Marzo2015", "\"C:\\WINDOWS\\system32\\cmd.exe\"",
				"\"/c a.exe 1\"" };
		try {
			File wrkDir = new File("c:\\procesos");

			Process process = Runtime.getRuntime().exec(cmdArray, null, wrkDir);
			printStdout(process.getInputStream());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}// testRunHardcodedCmd

	private static void runPsExecGetPidAndExitCode() {
		// String command = "testFiles\\a.exe";
		try {
			String command1 = "psexec -u ACCUSYSARGBSAS\\martin.zaragoza -p Marzo2015 D:\\Temp\\testScripts\\a.exe a";
			String[] cmd1 = { "psexec", "-u", "ACCUSYSARGBSAS\\martin.zaragoza", "-p", "Marzo2015", "D:\\Temp\\testScripts\\a.exe", "a" };
			Process process1 = Runtime.getRuntime().exec(cmd1, null, null);

			String command2 = "psexec -u ACCUSYSARGBSAS\\martin.zaragoza -p Marzo2015 D:\\Temp\\testScripts\\a.exe f";
			Process process2 = Runtime.getRuntime().exec(command2);

			int exitVal1 = process1.waitFor();
			int exitVal2 = process2.waitFor();
			System.out.println("exitVal1=" + exitVal1);
			System.out.println("exitVal2=" + exitVal2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}// runPsExecGetPidAndExitCode

	private static void tryRunCmdGetPid() {
		String command = "D:\\Temp\\testScripts\\a.exe a";
		// String command = "testFiles\\a.exe";
		try {
			Process process1 = Runtime.getRuntime().exec(command);
			InputStream inputStream = process1.getInputStream();

			int exitVal = process1.waitFor();

			printStdout(inputStream);
			System.out.println("exit code=" + exitVal);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void tryPsExecGetPid() throws IOException {

		Runnable runnable1 = new Runnable() {
			public void run() {
				/*
				 * psexec -u ACCUSYSARGBSAS\martin.zaragoza -p Marzo2015
				 * D:\Temp\testScripts\a.exe a
				 */
				String command = "psexec -u ACCUSYSARGBSAS\\martin.zaragoza -p Marzo2015 D:\\Temp\\testScripts\\a.exe a";
				Process process;
				try {
					process = Runtime.getRuntime().exec(command);
				} catch (IOException e1) {
					e1.printStackTrace();
					return;
				}

				try {
					int pid = getPid(process);
					System.out.println("pid=" + pid);
					printStdout(process.getInputStream());
					Thread.sleep(500);

					Process exec = Runtime.getRuntime().exec("echo %errorlevel%");
					printStdout(exec.getInputStream());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		List<Thread> threadList = new ArrayList<Thread>();
		threadList.add(new Thread(runnable1));

		for (Thread thread : threadList) {
			thread.start();
		}

		for (Thread thread : threadList) {
			try {
				thread.join();
			} catch (InterruptedException e) {
			}
		}
	}// tryPsExecGetPid

	public static int getPid(Process proceso) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field f = proceso.getClass().getDeclaredField("handle");
		f.setAccessible(true);
		long handl = f.getLong(proceso);

		Kernel32 kernel = Kernel32.INSTANCE;
		W32API.HANDLE handle = new W32API.HANDLE();
		handle.setPointer(Pointer.createConstant(handl));
		return kernel.GetProcessId(handle);
	}

	private static void tryRunAsUser() throws IOException {
		String command = "./lib/runAsUser.exe D:\\Temp\\testScripts martin.zaragoza@ACCUSYSARGBSAS Marzo2015 a.exe a";
		// String command = "testFiles\\a.exe";
		Process exec = Runtime.getRuntime().exec(command);
		InputStream inputStream = exec.getInputStream();

		printStdout(inputStream);
	}

	private static void printStdout(InputStream inputStream) throws IOException {
//		byte[] buffer = new byte[1024];
//		while (inputStream.read(buffer) > 0) {
//			System.out.println(new String(buffer, Charset.forName("UTF-8")));
//		}

	}
}// RunCmdTest

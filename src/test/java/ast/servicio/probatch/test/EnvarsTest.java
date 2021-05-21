package ast.servicio.probatch.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class EnvarsTest {
	public static void main(String[] args) throws IOException {
		String current = new java.io.File(".").getCanonicalPath();
		System.out.println("Current dir:" + current);
		String currentDir = System.getProperty("user.dir");
		System.out.println("Current dir using System:" + currentDir);

		//		String[] cmdarr = { "cmd", "/c", "D:\\ProBatch\\datosPrueba\\windows\\printvars.bat" };
		String[] cmdarr = { "cmd", "/c", "D:\\workspaces\\C++\\runasuser\\envars.exe" };
		String path = System.getenv("PATH");
		String[] envp = { "PBATCH=D:\\ProbatchJavaServicioWin", "PATH=" + path };

		runWithEmptyEnv(cmdarr);

		runWithNullEnv(cmdarr);

		runWithEnv(cmdarr, envp);

		runWithEnv(cmdarr, new String[] {"PBATCH=HOLA"});
	}

	private static void runWithEnv(String[] cmdarr, String[] envp) throws IOException {
		System.out.println();
		System.out.println("With some env:");
		Process process = Runtime.getRuntime().exec(cmdarr, envp, new File("D:\\ProBatch\\datosPrueba\\windows\\"));

		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		printOut(reader);
	}

	private static void runWithNullEnv(String[] cmdarr) throws IOException {
		System.out.println();
		System.out.println("null env:");
		Process process = Runtime.getRuntime().exec(cmdarr, null, new File("D:\\ProBatch\\datosPrueba\\windows\\"));
		//		Process process = Runtime.getRuntime().exec(cmdarr, envp , new File("D:\\ProBatch\\datosPrueba\\windows\\"));

		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		printOut(reader);
	}

	private static void runWithEmptyEnv(String[] cmdarr) throws IOException {
		System.out.println();
		System.out.println("Empty env:");
		Process process = Runtime.getRuntime().exec(cmdarr, new String[] {}, new File("D:\\ProBatch\\datosPrueba\\windows\\"));
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		printOut(reader);
	}

	private static void printOut(BufferedReader reader) throws IOException {
		while (true) {
			String line = reader.readLine();
			if (line == null) { break; }
			System.out.println(line);
		}
	}
}

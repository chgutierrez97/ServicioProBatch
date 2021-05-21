package ast.servicio.probatch.test.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class RunExecuteScriptTest {
	public void testRun() throws IOException {
		Runtime runtime = Runtime.getRuntime();
		String cmd = "/opt/ProbatchJavaDemonioLinux_V2.2/pbwin/lib/ExecuteScript.sh '/bin/bash /home/martin/bin/dummySleeper.sh 60 ;' 96555";
		Process process = runtime.exec(cmd);
		
		InputStream inputStream = process.getInputStream();
		String line = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		while( (line=br.readLine()) != null ){
			log(line);
		}
		
		log("end...");
	}// testReplaceAll

	private void log(String s) {
		System.out.println("RunExecuteScriptTest::"+s);
	}
	
	public static void main(String[] args) throws IOException {
		new RunExecuteScriptTest().testRun();
	}
}

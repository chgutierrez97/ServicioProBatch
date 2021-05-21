package ast.servicio.probatch.test.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FoxproCaller {
	
	
    public static void main(String[] args) throws IOException, InterruptedException {

//        String[] cmd = {"cmd","/C","fer_ok.exe"};
//        String[] cmd = {"D:\\vfp\\fer_ok.exe"};
        String[] cmd = {"D:\\vfp\\ftp_1_window.exe"};
//    	String[] cmd = {"D:\\vfp\\ftp_1_compileerr.exe"};


        Map<String, String> envMap = System.getenv();
        Set<Map.Entry<String, String>> envEntries = envMap.entrySet();
        List<String> envVars = new ArrayList<String>();
        for (Map.Entry<String, String> envEntry : envEntries) {
            envVars.add(String.format("%s=%s", envEntry.getKey(), envEntry.getValue()));
        }


        File dir = new File("D:\\vfp\\");
        final Process process = Runtime.getRuntime().exec(cmd, envVars.toArray(new String[]{}), dir);
//        final Process process = Runtime.getRuntime().exec(cmd);
        InputStream inputStream = process.getInputStream();
        InputStream errorStream = process.getErrorStream();
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        final BufferedReader bufferedErrorReader = new BufferedReader(new InputStreamReader(errorStream));


        Thread t1 = new Thread(new Runnable() {
            public void run() {
                String line;
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.setDaemon(true);
        t1.start();
        
        Thread t2 = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    System.out.println("Enviando enter...");
                    try {
                        enviarEnter(process);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t2.setDaemon(true);
        t2.start();

        int exitCode = process.waitFor();
        System.out.println("waitfor...");
        System.out.println("exitCode: " + exitCode);
        t1.join(1000);
        t2.join(1000);

    }

    private static void enviarEnter(Process process) throws IOException {
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream()));
        out.write("\\h");
        out.newLine();
        out.flush();
        out.write("\\q");
        out.newLine();
        out.flush();

        out.write("\r");
        out.newLine();
        out.flush();

        out.write("\r\n");
        out.newLine();
        out.flush();

        out.write("\n\r");
        out.newLine();
        out.flush();
        
    }
}

package ast.servicio.probatch.test.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SystemUtils {

    private SystemUtils() {}

    public static long getPID() {
        String processName =
                java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(processName.split("@")[0]);
    }

    public static void main(String[] args) throws IOException {
        long pid = getPID(); // obtengo el pid de la VM de java
        Runtime runtime = Runtime.getRuntime();

        /*  corro un proceso de foxpro ------------------------------------------------------------------------------------ */
        String[] foxprocmd = {"D:\\vfp\\ftp_1_window.exe"};
        Map<String, String> envMap = System.getenv();
        Set<Map.Entry<String, String>> envEntries = envMap.entrySet();
        List<String> envVars = new ArrayList<String>();
        for (Map.Entry<String, String> envEntry : envEntries) {
            envVars.add(String.format("%s=%s", envEntry.getKey(), envEntry.getValue()));
        }
        File dir = new File("D:\\vfp\\");
        final Process foxproProcess = Runtime.getRuntime().exec(foxprocmd, envVars.toArray(new String[]{}), dir);

        /* Busco los procesos hijos de la vm ------------------------------------------------------------------------------ */

        String[] command = {"cmd.exe", "/C", "wmic process where (parentprocessid= " + pid + " ) get processid /VALUE"};
        Process process = runtime.exec(command);

        String newLine = null;
        String childPid = null;
        BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));

        List<Integer> childPids = new ArrayList<Integer>();
        while ((newLine = buffer.readLine()) != null) {
            newLine = newLine.trim();
            if ("".equals(newLine)) continue;

            if (newLine.contains("ProcessId=")) {
                childPid = newLine.split("ProcessId=")[1];
                System.out.println("proceso hijo encontrado: " + childPid);
                childPids.add(Integer.parseInt(childPid));
            }
        }
    }

}
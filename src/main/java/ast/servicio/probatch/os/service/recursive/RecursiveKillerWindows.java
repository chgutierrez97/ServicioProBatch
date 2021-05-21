package ast.servicio.probatch.os.service.recursive;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecursiveKillerWindows implements RecursiveKiller {
    private Logger logger = LoggerFactory.getLogger(RecursiveKillerSolaris.class);

    public void initKiller(int pid) {
        try {
            __run(pid);
        } catch (Exception e) {
            logger.debug("Error al eliminar pid " + e.getMessage());
        }
    }

    private void __run(int currPid) throws Exception {
        String[] command = {"cmd.exe", "/C", "wmic process where (parentprocessid= " + currPid + " ) get processid /VALUE"};
        Runtime runtime = Runtime.getRuntime();
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
                logger.debug("proceso hijo encontrado: {}", childPid);
                childPids.add(Integer.parseInt(childPid));
            }
        }

        String killCmd = String.format("TASKKILL /F /T /PID %d", currPid);
        logger.debug("Ejecutando {}", killCmd);
        runtime.exec(killCmd);

        // Vuelvo a iterar para todos los procesos hijos
        for (Integer nextPid : childPids) __run(nextPid);
    }
}

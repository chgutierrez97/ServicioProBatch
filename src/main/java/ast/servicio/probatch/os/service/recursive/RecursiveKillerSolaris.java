package ast.servicio.probatch.os.service.recursive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.threads.ListenerProceso;

public class RecursiveKillerSolaris implements RecursiveKiller {
    /**
     * Indica si se debe imprimir el listado de procesos mientras se manda a matar a alguno
     */
    private static boolean DEBUG_PS_PRINT = false;
    private Logger logger = LoggerFactory.getLogger(RecursiveKillerSolaris.class);

    public void initKiller(int mainPid) {
        try {
            logger.debug("initKiller::mainPid: " + mainPid);

			/* OPCION 1 */
            // if (ListenerProceso.flagCobolError) {
            // __runKillLastPid(mainPid);
            // }
            // __run(mainPid);

			/* OPCION 2 */
            killProcessAndDescendants(mainPid);

			/* OPCION 3 NO FUNCIONA... */
            // killProcessGroup(mainPid);
        } catch (Exception e) {
            System.err.println("error: " + e.toString());
        }
    }

    private void killProcessGroup(int mainPid) throws IOException {
        String[] cmd = {"/bin/sh", "-c", "ps -efo pid,pgid | grep " + mainPid};

        Runtime rt = Runtime.getRuntime();
        Process psProcess = rt.exec(cmd);

        String line = "";
        BufferedReader psReader = new BufferedReader(new InputStreamReader(psProcess.getInputStream()));

        line = psReader.readLine();
        line = line.trim().replaceAll(" +", " ");
        logger.debug("killPgid::line: " + line);
        String[] splitLine = line.split(" ");
        int parsedGroupPid = Integer.valueOf(splitLine[1]);
        logger.debug("killPgid::parsedGroupPid: " + parsedGroupPid);

        while ((line = psReader.readLine()) != null) {
            // AGOTAMOS EL STREAM DEL COMANDO ANTERIOR
        }

        executeAndLogOutput("kill -9 " + parsedGroupPid);
    }

    /* FUNCIONA listando los procesos y esperando 5 segundos luego... */
    private void killProcessAndDescendants(int mainPid) throws IOException, InterruptedException {
        // String processListCmd = "ps -ef | grep press | grep -v 'grep'";
        String processListCmd = "ps -ef | grep -v 'grep'";

        if (DEBUG_PS_PRINT) {
            logger.debug("*********************************************************************************************************");
            logger.debug("LISTANDO LOS PROCESOS...");
            executeAndLogOutput(processListCmd);
            logger.debug("*********************************************************************************************************");
        }

        int waitSecs = 5;
        Thread.sleep(1000 * waitSecs);

        if (DEBUG_PS_PRINT) {
            logger.debug("*********************************************************************************************************");
            logger.debug("PROCESOS LUEGO DE ESPERAR " + waitSecs + "...");
            executeAndLogOutput(processListCmd);
            logger.debug("*********************************************************************************************************");
        }

        Collection<Integer> processDescendants = getProcessDescendants(mainPid);
        logger.debug("DESCENDIENTES DEL PROCESO " + mainPid + " : " + processDescendants);
        for (Integer pid : processDescendants) {
            logger.debug("MATANDO " + pid);
            String cmd = "kill -9 " + pid;
            executeAndLogOutput(cmd);
        }
        logger.debug("MATANDO " + mainPid);
        executeAndLogOutput("kill -9" + mainPid);

        if (DEBUG_PS_PRINT) {
            logger.debug("*********************************************************************************************************");
            logger.debug("LISTADO DE PROCESOS LUEGO DE MATAR A " + mainPid + " Y SUS DESCENDIENTES...");
            executeAndLogOutput(processListCmd);
            logger.debug("*********************************************************************************************************");
        }

        ListenerProceso.flagCobolError = false;
    }

    /**
     * Obtiene los procesos "descendientes" del proceso pasado por parametro.
     *
     * @param currPid Id de proceso a obtener sus descendientes.
     * @return descendientes del proceso.
     * @throws IOException En caso que ocurra un error al ejecutar el comando o
     */
    private Collection<Integer> getProcessDescendants(int currPid) throws IOException {
        /* CON EL SIGUIENTE COMANDO BUSCO OBTENER LOS PID HIJOS Y PADRE DE currPid */
        String[] cmd = {"/bin/sh", "-c", "ps -efo pid,ppid | grep " + currPid};
        // logger.debug("getProcessDescendants::cmd: " + Arrays.toString(cmd));
        logger.debug("BUSCANDO HIJOS DIRECTOS DEL PROCESO " + currPid);

        Runtime rt = Runtime.getRuntime();
        Process psProcess = rt.exec(cmd);

        String line = "";
        BufferedReader psReader = new BufferedReader(new InputStreamReader(psProcess.getInputStream()));
        List<Integer> childrenPids = new ArrayList<Integer>();

        logger.debug("getProcessDescendants::currPid: " + currPid);

		/*
		 * LA RESPUESTA DEL COMANDO TENDRA EL FORMATO "ZZZ YYY" SIENDO EL PRIMER VALOR EL PID Y EL SEGUNDO EL PPID.
		 */
        while ((line = psReader.readLine()) != null) {
            line = line.trim().replaceAll(" +", " ");
            logger.debug("getProcessDescendants::line: " + line);

            String[] splitLine = line.split(" ");

            int parsedPid = Integer.valueOf(splitLine[0]);
            int parsedParentPid = Integer.valueOf(splitLine[1]);

            if (currPid == parsedParentPid) {
                logger.debug("getProcessDescendants::adding child: " + parsedPid);
                childrenPids.add(parsedPid);
            }
        }

        if (childrenPids.isEmpty()) {
            logger.debug("getProcessDescendants::NO SE ENCONTRARON HIJOS DEL PROCESO " + currPid);
            return new ArrayList<Integer>();
        } else {
            logger.debug("getProcessDescendants::HIJOS DEL PROCESO " + currPid + " : " + childrenPids);

            ArrayList<Integer> descendants = new ArrayList<Integer>();
            descendants.addAll(childrenPids);

            for (Integer childPid : childrenPids) {
                Collection<Integer> childDescendants = getProcessDescendants(childPid);
                descendants.addAll(childDescendants);
            }

            return descendants;
        }
    }

    private void __runKillLastPid(int currPid) throws IOException {

        String[] cmd = {"/bin/sh", "-c", "ps -efo pid,ppid | grep " + currPid};
        logger.debug("__runKillLastPid::cmd: " + Arrays.toString(cmd));

        Runtime rt = Runtime.getRuntime();
        Process process = rt.exec(cmd);

        String line = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        Integer last_pid = 0;
        logger.debug("__runKillLastPid::currPid: " + currPid);

        while ((line = br.readLine()) != null) {
            line = line.trim().replaceAll(" +", " ");
            logger.debug("__runKillLastPid::line: " + line);

            String[] splitLine = line.split(" ");

            int parsedPid = Integer.valueOf(splitLine[0]);
            logger.debug("__runKillLastPid::parsedPid: " + parsedPid);

            if (currPid > parsedPid) {
                logger.debug("__runKillLastPid::break!");
                break;
            }

            if (currPid == parsedPid) {
                logger.debug("__runKillLastPid::continue!");
                continue;
            } else {
                last_pid = parsedPid;
            }
        }

        if (last_pid == 0) {
            logger.debug("__runKillLastPid::killing pid " + currPid);
            String killCmd = "kill -9 " + currPid;

            executeAndLogOutput(killCmd);

            ListenerProceso.flagCobolError = false;
            return;
        } else {
            __runKillLastPid(last_pid);

        }
    }// __run

    /**
     * Mata un proceso y a todos sus hijos.
     *
     * @param currPid Proceso a eliminar junto a sus hijos.
     * @throws IOException En caso que ocurra un error en la ejecucion de los comandos del sistema operativo o en la lectura del output de los mismos.
     */
    private void __run(int currPid) throws IOException {
        String[] cmd = {"/bin/sh", "-c", "ps -efo pid,ppid | grep " + currPid};
        // String[] cmd = { "/bin/sh", "-c", "ps -efo pid,ppid | grep " + currPid + " | cut -d\" \" -f2" };
        logger.debug("__run::cmd: " + Arrays.toString(cmd));

        Runtime rt = Runtime.getRuntime();
        Process psProcess = rt.exec(cmd);

        String line = "";
        BufferedReader psReader = new BufferedReader(new InputStreamReader(psProcess.getInputStream()));
        List<Integer> childrenPids = new ArrayList<Integer>();

        logger.debug("__run::currPid: " + currPid);

        while ((line = psReader.readLine()) != null) {
            line = line.trim().replaceAll(" +", " ");
            logger.debug("__run::line: " + line);

            String[] splitLine = line.split(" ");

            int parsedPid = Integer.valueOf(splitLine[0]);
            int parsedParentPid = Integer.valueOf(splitLine[1]);
            logger.debug("__run::parsedPid: " + parsedPid);

            if (currPid == parsedParentPid) {
                childrenPids.add(parsedPid);
            }
        }

        logger.debug("__run::killing " + currPid);
        String killCmd = "kill -9 " + currPid;
        executeAndLogOutput(killCmd);

        if (childrenPids.isEmpty()) {
            logger.debug("__run::no children pids found for process " + currPid);
        } else {
            logger.debug("__run::process " + currPid + " children: " + childrenPids);
            for (Integer nextPid : childrenPids) {
                __run(nextPid);
            }
        }
    }// __run

    /**
     * Ejecuta un comando y loguea el output del mismo.
     *
     * @param scmd Comando a ejecutar
     * @throws IOException En caso que ocurra una excepcion en la ejecucion del comando o la lectura del output.
     */
    private void executeAndLogOutput(String scmd) throws IOException {
        String[] cmd = {"/bin/sh", "-c", scmd};
        Process process = Runtime.getRuntime().exec(cmd);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String output;
        while ((output = reader.readLine()) != null) {
            logger.debug(output);
        }
    }
}

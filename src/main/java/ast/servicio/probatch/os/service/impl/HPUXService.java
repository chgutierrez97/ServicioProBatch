package ast.servicio.probatch.os.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.StringCommands;
import ast.servicio.probatch.util.Utils;

public class HPUXService extends UnixService {
	public static Logger logger = LoggerFactory.getLogger(HPUXService.class);

	public HPUXService() {
		super();
	}

	@Override
	public String[] getExecuteCommand(ParametrosProceso parametroP) {
		String[] ejecucion = { "" };

		ejecucion[0] = createStringCommand(parametroP);

		ejecucion[0] = ejecucion[0].concat(" " + parametroP.getId());
		this.idParametroProceso = parametroP.getId();
		logger.info("COMANDO CONSTRUIDO: " + ejecucion[0]);
		return ejecucion;
	}

	/**
	 * Crea la base del comando a ejecutar de la forma:<br/>
	 * <strong>./lib/ExecuteScript.sh '/bin/su syspro -c
	 * "cd /bcps3/ctacte/batch/menus/ ;. /sybase/SYBASE.sh; DSQUERY=SYBFACE;export DSQUERY ; . /sybase/setenv.sh  ; export login=syspro pwd=syspro; set ; id ;"
	 * '</strong> <br/>
	 * (el argumento principal de ExecuteScript se encuentra completamente
	 * escapeado).
	 * 
	 * @param parametroP
	 *            - Informacion para crear el argumento (parametros, usuarios,
	 *            datos a especificar, etc.)
	 * @return String comando base sin Pid al final del mismo.
	 */
	private String createStringCommand(ParametrosProceso parametroP) {
		int debugMode = ServicioAgente.cfg.getDebugMode();
		StringBuffer cp = new StringBuffer();

		// /bin/su syspro -c 'cd /prueba ;. /sybase/SYBASE.sh;
		// DSQUERY=SYBFACE;export DSQUERY ; . /sybase/setenv.sh ; export
		// login=syspro pwd=syspro123; exec
		// /sqr85/SQR/bin/SQR/Server/Sybase/bin/sqr /prueba/testeo4.sqr
		// $login/$pwd -RT 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 ;'

		cp.append(ServicioAgente.cfg.getShellScript());
		cp.append(StringCommands.STRING_EMPTY.toString());

		cp.append(StringCommands.IMPERSONALIZATION_USER_FULL.toString());
		cp.append(StringCommands.STRING_EMPTY.toString());
		cp.append(parametroP.getUsuario().getNombre());// syspro
		cp.append(StringCommands.STRING_EMPTY.toString());
		cp.append(StringCommands.COMMAND_OPTION.toString());
		/*
		 * HASTA AQUI SE FORMA ALGO COMO: ./lib/ExecuteScript.sh '/bin/su syspro
		 * -c
		 */

		cp.append(StringCommands.STRING_EMPTY.toString());
		cp.append(StringCommands.QUOTE_SIMPLE.toString());
		cp.append(StringCommands.CHDIR.toString());
		cp.append(StringCommands.STRING_EMPTY.toString());
		cp.append(parametroP.getChdir());
		cp.append(StringCommands.STRING_EMPTY.toString());
		cp.append(StringCommands.PUNTO_COMA.toString());
		/*
		 * HASTA AQUI SE TIENE ALGO COMO: ./lib/ExecuteScript.sh '/bin/su syspro
		 * -c "cd /bcps3/ctacte/batch/menus/ ;
		 */

		String envCmdTemp = ServicioAgente.cfg.getEnvCmd();
		String envCmd = (envCmdTemp == null || envCmdTemp.contentEquals("")) ? StringCommands.SYBASEENV.toString() : envCmdTemp;
		if (envCmd != null && envCmd.contentEquals("") == false && debugMode != 5) {
			cp.append(envCmd);
		}

		cp.append(StringCommands.STRING_EMPTY.toString());
		cp.append(StringCommands.PUNTO_COMA.toString());
		cp.append(StringCommands.STRING_EMPTY.toString());
		cp.append(StringCommands.EXPORT.toString());// export
		cp.append(StringCommands.STRING_EMPTY.toString());
		cp.append(parametroP.getVarEntornoString());
		cp.append(StringCommands.PUNTO_COMA.toString());
		cp.append(StringCommands.STRING_EMPTY.toString());
		cp.append(StringCommands.EXEC.toString());// exec

		cp.append(StringCommands.STRING_EMPTY.toString());
		cp.append(Utils.getArrayCommands(parametroP));
		cp.append(StringCommands.QUOTE_SIMPLE.toString());
		/* fin de construccion del comando */

		return cp.toString();
	}// createStringCommand

	@Override
	public int getPid(Process proceso) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		return getPidFromFile(proceso);
	}// getPid

	private int getPidFromFile(Process proceso) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String pidFileName = "pid_" + this.idParametroProceso;

		File pidFile = new File(pidFileName);
		int timeCount = 0;
		int sleepTime = 100;

		while (true) {
			if (pidFile.exists())
				break;

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			timeCount += sleepTime;
			if (timeCount >= 3000) {
				logger.error("NO EXISTE ARCHIVO DE PID: " + pidFileName);
				throw new IllegalArgumentException("No existe archivo " + pidFileName);
			}
		}// while (true)

		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(pidFile)));
			String s_pid = bufferedReader.readLine();
			// l.debug("HPUXService::getPid::s_pid=" + s_pid);
			bufferedReader.close();
			return Integer.parseInt(s_pid);
		} catch (Exception e) {
			return -1;
		}
	}// getPid

	@Override
	public String getKillCommand(int pid) {
		return "pkill -TERM -P " + pid;
	}

}// HPUXService

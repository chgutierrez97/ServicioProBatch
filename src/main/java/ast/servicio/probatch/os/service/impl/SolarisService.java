//package ast.servicio.probatch.os.service.impl;
//
//import java.util.Iterator;
//
//import ast.servicio.probatch.domain.Atributo;
//import ast.servicio.probatch.domain.ParametrosProceso;
//
//public class SolarisService extends LinuxService {
//
//	public static final String IMPERSONALIZACION_USER = "su - ";
//	
//	public SolarisService() {
//		super();
//	}
//
//	public String[] getExecuteCommand(ParametrosProceso parametroP) {
//	
//		
//		StringBuilder parametros = new StringBuilder();
//
//		parametros.append(IMPERSONALIZACION_USER + parametroP.getUsuario().getNombre());
//		parametros.append(" -c 'cd " + parametroP.getChdir() + "; ");
//		parametros.append("exec " + parametroP.getComando());
//		if (parametroP.getArgumentos() != null) {
//			for (Iterator<Atributo> iterator = parametroP.getArgumentos().iterator(); iterator.hasNext();) {
//				Atributo atributo = iterator.next();
//				parametros.append(" ");
//				parametros.append(atributo.getValor());
//			}
//		}
//		parametros.append("'");
//		
//		String[] comando = {"/bin/sh", "-c",parametros.toString()};
//
//		return comando;
//
//	}
//}

package ast.servicio.probatch.os.service.impl;

import java.util.Collection;
import java.util.Iterator;

import ast.servicio.probatch.service.ServicioAgente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.util.StringCommands;

public class SolarisService extends LinuxService {
	public static Logger logger = LoggerFactory.getLogger(SolarisService.class);

	public SolarisService() {}

	public String[] getExecuteCommand(ParametrosProceso parametroP) {
		StringBuilder cmdString = new StringBuilder();

		// su - user -c 'cd $DIR;
		cmdString.append(String.format("su - %s -c 'cd %s; ", parametroP.getUsuario().getNombre(), parametroP.getChdir()));

		// su - user -c 'cd $DIR; $KEY1=$VAL1; export $KEY1...
		if (parametroP.getEntorno() != null && !parametroP.getEntorno().isEmpty()) {
			Iterator<Atributo> envVarsIt = parametroP.getEntorno().iterator();
			Atributo firstEnvVar = envVarsIt.next();
			cmdString.append(String.format("%s=%s ; export %s", firstEnvVar.getNombre(), firstEnvVar.getValorMostrar(), firstEnvVar.getNombre()));

			while (envVarsIt.hasNext()) {
				Atributo envVar = envVarsIt.next();
				cmdString.append(String.format(" ; %s=%s ; export %s", envVar.getNombre(), envVar.getValorMostrar(), envVar.getNombre()));
			}

			cmdString.append(" ; ");
		}

		// TODO : DEBERIAMOS USAR exec ? Creo que originalmente se usaba para HPUX
		// su - user -c 'cd $DIR; $KEY1=$VAL1; export $KEY1... ; exec $CMD $ARG1 $ARG2...'
		cmdString.append(String.format("exec %s", parametroP.getComando()));
		if (parametroP.getArgumentos() != null) {
			Collection<Atributo> args = parametroP.getArgumentos();
			for (Atributo arg : args) cmdString.append(String.format(" %s", arg.getValor()));
		}
		cmdString.append("'");

		logger.debug("COMANDO CONSTRUIDO PARA SOLARIS: " + cmdString);

		// /bin/sh -c su - user -c 'cd $DIR; $KEY1=$VAL1; export $KEY1... ; exec $CMD $ARG1 $ARG2...'
		String terminal = ServicioAgente.cfg.getDefTerminal();
		String[] comando = {terminal, "-c", cmdString.toString()};

		return comando;
	}

	/**
	 * Genera el comando
	 *
	 * @param parametroP
	 * @return
	 * @deprecated
	 */
	public String[] getExecuteCommand_old(ParametrosProceso parametroP) {
		StringBuilder parametros = new StringBuilder();


		// su - user
		parametros.append(StringCommands.IMPERSONALIZATION_USER.toString() + StringCommands.HYPHEN.toString());
		parametros.append(parametroP.getUsuario().getNombre());
		parametros.append(StringCommands.STRING_EMPTY.toString());


		// su - user -c 'cd $DIR;
		parametros.append(StringCommands.COMMAND_OPTION.toString());
		parametros.append(StringCommands.STRING_EMPTY.toString());
		parametros.append("'cd " + parametroP.getChdir() + StringCommands.PUNTO_COMA.toString());


		// su - user -c 'cd $DIR; exec $COMMAND
		parametros.append(StringCommands.EXEC.toString() + parametroP.getComando());
		if (parametroP.getArgumentos() != null) {
			for (Iterator<Atributo> iterator = parametroP.getArgumentos().iterator(); iterator.hasNext(); ) {
				Atributo atributo = iterator.next();
				parametros.append(StringCommands.STRING_EMPTY.toString());
				parametros.append(atributo.getValor());
			}
		}

		// su - user -c 'cd $DIR; exec $COMMAND $ARGS...'
		parametros.append(StringCommands.QUOTE_SIMPLE.toString());

		String[] comando = {StringCommands.COMMAND_MAIN.toString(), StringCommands.COMMAND_OPTION.toString(), parametros.toString()};

		return comando;
	}
}

package ast.servicio.probatch.os.service.impl;

import java.util.Arrays;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.util.StringCommands;

public class UnixService extends LinuxService {
	public static Logger logger = LoggerFactory.getLogger(UnixService.class);

	public UnixService() {
		super();
	}

	public String[] getExecuteCommand(ParametrosProceso parametroP) {
		StringBuilder parametros = new StringBuilder();

		/**
		 * Se Busca armar la siguiente linea de comando
		 * 
		 * bin/su - usuario -c 'cd path ; export variable_entorno ; exec
		 * argumento1 argumento2 argumentoN'
		 */

		// su -
		parametros.append(StringCommands.IMPERSONALIZATION_USER_FULL.toString() + StringCommands.HYPHEN.toString());
		// syspro
		parametros.append(parametroP.getUsuario().getNombre());
		//
		parametros.append(StringCommands.STRING_EMPTY.toString());
		// -c
		parametros.append(StringCommands.COMMAND_OPTION.toString());
		//
		parametros.append(StringCommands.STRING_EMPTY.toString());
		// 'cd $DIR;
		parametros.append("'cd " + parametroP.getChdir() + StringCommands.PUNTO_COMA.toString());
		if (parametroP.getVarEntornoString() != null) {
			parametros.append(StringCommands.EXPORT.toString() + parametroP.getVarEntornoString() + StringCommands.PUNTO_COMA.toString());
		}
		parametros.append(StringCommands.EXEC.toString() + parametroP.getComando());
		if (parametroP.getArgumentos() != null) {
			for (Iterator<Atributo> iterator = parametroP.getArgumentos().iterator(); iterator.hasNext();) {
				Atributo atributo = iterator.next();
				parametros.append(StringCommands.STRING_EMPTY.toString());
				parametros.append(atributo.getValor());
			}
		}
		parametros.append(StringCommands.QUOTE_SIMPLE.toString());

		String[] comando = { StringCommands.COMMAND_MAIN.toString(), StringCommands.COMMAND_OPTION.toString(), parametros.toString() };

		logger.info("COMANDO CONSTRUIDO: " + Arrays.toString(comando));

		return comando;
	}

}

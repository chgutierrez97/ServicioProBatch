package ast.servicio.probatch.test;

import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.os.service.impl.WindowsService;

public class BuscarUsuarioPermisosTest {

	/*	ARGS
	 * franco.milanese 
	 * r 
	 * "C:\Program Files\CCleaner\\" 
	 * accusysargbsas.local
	 */

	public static void main(String[] args) throws MensajeErrorException {

		WindowsService windowsService = new WindowsService();

		windowsService.buscarUsuarioPermisos(args[0], args[1], args[2], args[3]);

	}

}

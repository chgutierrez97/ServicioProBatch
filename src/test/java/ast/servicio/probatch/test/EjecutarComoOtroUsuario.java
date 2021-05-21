package ast.servicio.probatch.test;

import java.io.IOException;

import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;

import ast.servicio.probatch.os.service.OsServiceFactory;

public class EjecutarComoOtroUsuario {

	/**
	 * @param args
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws ObjectDoesNotExistException
	 * @throws InterruptedException
	 * @throws ErrorCompletingRequestException
	 * @throws AS400SecurityException
	 */
	public static void main(String[] args) throws IOException, SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
			AS400SecurityException, ErrorCompletingRequestException, InterruptedException, ObjectDoesNotExistException {

		Process proceso = OsServiceFactory.getOsService().executeCommand("D:\\Proyectos\\ProBatch\\CPAU.EXE -u matias.brino -p SaintSeiya14 -ex notepad.exe");

		int pid = OsServiceFactory.getOsService().getPid(proceso);

		System.out.println("pid: " + pid);

	}

}

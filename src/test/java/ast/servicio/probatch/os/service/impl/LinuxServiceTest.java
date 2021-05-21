package ast.servicio.probatch.os.service.impl;

//import org.junit.Assert;
//import org.junit.Test;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.ParametrosProceso;

//import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

public class LinuxServiceTest {
//	@Test
//	public void reemplazaExpRegArchivo() throws Exception {
//		LinuxService linuxService = new LinuxService();
//		String nombre = "/opt/fecha.*";
//		String reemplazo = linuxService.reemplazaExpRegArchivo(nombre);
//		System.out.println(reemplazo);
//	}

//	@Test
//	public void resuelveVariablesDeSistema() throws Exception {
//		LinuxService linuxService = new LinuxService();

//		String v1 = System.getenv("OS");
//		String v2 = System.getenv("APPDATA");
//		String v3 = System.getenv("COMPUTERNAME");

		//String out = linuxService.resuelveVariablesDeSistema("Hola $OS como $APPDATA estas $COMPUTERNAME");

		//String expected = String.format("Hola %s como %s estas %s", v1, v2, v3);

		//Assert.assertEquals(expected, out);

	//}

	//@Test
	//public void testGetExecuteCommand() throws Exception {
		//ParametrosProceso parametrosP = new ParametrosProceso();
		//parametrosP.setId("96555");
		//parametrosP.setTs(999L);
		//parametrosP.setNombre("TEST");
		//parametrosP.setCategoria("batch");
		//parametrosP.setClase("");
		//parametrosP.setChdir("/bcps5/ctacte/batch/menus");
		//parametrosP.setComando("/respaldos/sqr11/EPMSystem11R1/products/biplus/bin/SQR/Server/Sybase/bin/sqr");
		//parametrosP.setUsuario(new Atributo("syspro", "syspro"));
		// parametrosP.setResultado(resultado);
		//parametrosP.setEntorno(Collections.unmodifiableCollection(Arrays.asList(new Atributo("login", "syspro"), new Atributo("pwd", "syspro", "oculto"))));
		// parametrosP.setInterfaces(interfaces);
		//parametrosP.setPatrones(Collections.unmodifiableCollection(Arrays.asList(new Atributo("ignorar", "borraseabp.sh", "re"),
			//	new Atributo("ignorar", "[20;28H", "literal"), new Atributo("ignorar", "cannot open", "re"))));
		//parametrosP.setArgumentos(Collections.unmodifiableCollection(Arrays.asList(new Atributo("", "../objetos/ccopmone.sqr"),
			//	new Atributo("", "$login/$pwd -RT "), new Atributo("", "-F/bcps5/ctacte/batch/listados/ccopmone_%d%m%y_%H%M.lis"), new Atributo("", "1"),
				//new Atributo("", "06/06/2018"), new Atributo("", "4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20"))));

		
		//LinuxService ls = new LinuxService();
		
		//String[] executeCommand = ls.getExecuteCommand(parametrosP);
		//String[] expected = new String[] {"/bin/sh","-c","/bin/su - syspro -c 'cd /bcps5/ctacte/batch/menus;  export login=syspro pwd=syspro; /respaldos/sqr11/EPMSystem11R1/products/biplus/bin/SQR/Server/Sybase/bin/sqr ../objetos/ccopmone.sqr $login/$pwd -RT  -F/bcps5/ctacte/batch/listados/ccopmone_%d%m%y_%H%M.lis 1 06/06/2018 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20'"};
		
		//assertEquals(3, executeCommand.length);
		//assertEquals(expected[0], executeCommand[0]);
		//assertEquals(expected[1], executeCommand[1]);
		//assertEquals(expected[2], executeCommand[2]);

	//}

}
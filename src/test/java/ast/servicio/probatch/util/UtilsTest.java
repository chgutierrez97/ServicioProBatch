package ast.servicio.probatch.util;
/*
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.Before;
//import org.junit.Test;

import ast.servicio.probatch.os.service.OsService;
import ast.servicio.probatch.os.service.impl.WindowsService;*/

public class UtilsTest {
/*
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testObtenerCadenas() {
		String str = "<validar><directorio id=\"d0\" usuario=\"probatch@cfa\" permiso=\"r\" path=\"E:\\tq\\Scheduler.NET\\scheduler.application\" /><directorio id=\"d1\" usuario=\"probatch@cfa\" permiso=\"r\" path=\"E:\\tq\\Scheduler.NET\\scheduler.application\\\" /></validar>";
		List<String> obtenerCadenas = obtenerCadenas(str, "" + (char) 13 + (char) 10);
	}

	public static List<String> obtenerCadenas(String cadena, String delim) {
		List<String> lista = new ArrayList<String>();
		StringTokenizer tokens = new StringTokenizer(cadena, delim);
		while (tokens.hasMoreElements()) {
			lista.add(tokens.nextToken().trim());
		}
		return lista;

	}

	@Test
	public void testObtenerOcultos() {
		byte[] xorstr = Utils.xorstr("MPBKeyED", "1041671", "%27%D5%6F%84%E5%A9%4E%F8%29%68");
		System.out.println(new String(xorstr));
	}

	@Test
	public void testArchivosNoEncontrados() {
		String cadena = "\\\\srv0028\\Releases_Desa\\hola.txt";
		String resultado = "";
		List<String> lista = obtenerCadenas(cadena, ";");
		OsService service = new WindowsService();
		for (String texto : lista) {
			texto = texto.replace("%d", Utils.seccionaFecha("dd"));
			texto = texto.replace("%m", Utils.seccionaFecha("MM"));
			texto = texto.replace("%y", Utils.seccionaFecha("yy"));
			texto = texto.replace("%Y", Utils.seccionaFecha("yyyy"));
			if (texto.contains("*")) {
//				texto = OsServiceFactory.getOsService().reemplazaExpRegArchivo(texto);
				texto = service.reemplazaExpRegArchivo(texto);
				System.out.println(texto);
			}
			if (!new File(texto).exists())
				resultado = resultado + texto + ";";
		}
		if (resultado.endsWith(";")) {
			resultado = resultado.substring(0, resultado.length() - 1);
		}
		System.out.println(resultado);
	}
*/
}

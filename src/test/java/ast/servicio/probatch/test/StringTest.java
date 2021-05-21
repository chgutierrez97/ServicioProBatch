package ast.servicio.probatch.test;
/*
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import junit.framework.Assert;
import junit.framework.TestCase;*/

public class StringTest/* extends TestCase*/ {
/*	public void testSplit() {
		String line = "  976   975";
		line = line.trim().replaceAll(" +", " ");
		String[] split = line.split(" ");
		System.out.println("" + split[0] + "-" + split[1]);
		System.out.println(Arrays.toString(split));

		String s = "ProcessId=5936 ";
		System.out.println( Arrays.asList(s.split("ProcessId=")[1].trim()) );
	}

	public void testLastSegment() {
		String permissions = "(OI)(CI)(IO)F";
		System.out.println(permissions.replaceAll(Pattern.quote("(") + ".+" + Pattern.quote(")"), "").trim());

		String someText = "holaComoEstas";
		System.out.println(Arrays.toString(someText.split("")));

		{
			boolean checkOk = true;
			String usrPermissions = "rwx";
			String permissionsToCheck = "xr";
			String[] splitPermissionsToCheck = permissionsToCheck.split("");
			for (String permissionToCheck : splitPermissionsToCheck) {
				if (!"".equals(permissionToCheck) && !usrPermissions.contains(permissionToCheck)) {
					checkOk = false;
				}
			}
			Assert.assertTrue(checkOk);
		}
		
		{
			boolean checkOk = true;
			String usrPermissions = "rw";
			String permissionsToCheck = "xr";
			String[] splitPermissionsToCheck = permissionsToCheck.split("");
			for (String permissionToCheck : splitPermissionsToCheck) {
				if (!"".equals(permissionToCheck) && !usrPermissions.contains(permissionToCheck)) {
					checkOk = false;
				}
			}
			Assert.assertFalse(checkOk);
		}
		
		Assert.assertTrue("hola".contains(""));
	}

	public void testReadLineCalcs() throws IOException {
		String path = "D:\\workspaces\\meta\\factory";
		Process p = Runtime.getRuntime().exec(new String[] { "cmd", "/c", "cacls", path });
		// Se obtiene el stream de salida del programa
		InputStream is = p.getInputStream();
		// Se prepara un bufferedReader para poder leer la salida más
		// comodamente.
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		// Se lee la primera linea

		String line = "";

		while ((line = br.readLine()) != null) {
			System.out.println(String.format("Line: %s", line));

			String sline = line.replaceFirst(Pattern.quote(path), "").trim();

			while ((StringUtils.countMatches(line, "\\") - 1) > 0) {
				line = line.substring(line.indexOf("\\") + 1, line.length());
			}
		}
	}*/
}

package ast.servicio.probatch.test;

import java.io.UnsupportedEncodingException;

import ast.servicio.probatch.util.Utils;

public class pruebaValidacion {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			// ServicioAgente probatch = new
			// ServicioAgente("probatch-6666.conf");

			/*
			 * el string encriptado se genera a partir de ejecutar Xor entre un
			 * string y el id de proceso
			 */

			// String encriptado = "%4A%D9%79%BD%68%DA%D9%EF%ED%3E%90";
			// String s_original = "accusys123*";
			// String id = "3398733";

			// String encriptado = "%2F%8C%09%9F%86%65%8F%C9%E5%5A";
			// String s_original = "ArcoCdui13";
			// String id = "3247";

			testDesencriptarClave();

			System.out.println(
					"------------------------------------------------------------------------------------------------------------------------------------------------------------");

			testEncriptarClave();

			System.out.println(
					"------------------------------------------------------------------------------------------------------------------------------------------------------------");

			testXorStr();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}// main

	private static void testXorStr() {
		String ts = "3434996";
		String key = "MPBKeyED";
		String clave = "%b0%7c%3f%94%80%13%41";

		byte[] xor = Utils.xorstr(ts, key, clave);

		System.out.println("testXorStr::xor=" + Utils.byte2hex(xor));
	}

	private static void testDesencriptarClave() throws UnsupportedEncodingException {
		String key = "MPBKeyED";
		// String id = "3434996";
		String id = "5555";
		// String s_palabraEncriptada = "%28%DB%3C%28%4A%C7";
		String s_palabraEncriptada = "%32%a8%ab%0f%8d";

		byte[] b_palabraDesencriptada = Utils.xorstr(key, id, s_palabraEncriptada);
		String s_palabraDesencriptada = new String(b_palabraDesencriptada, "UTF-8");

		System.out.println("s_palabraEncriptada:" + Utils.byte2hex(Utils.xorstr(key, id, Utils.byte2hex(b_palabraDesencriptada))));

		System.out.println("s_palabraDesencriptada: UTF8:" + s_palabraDesencriptada);

		s_palabraDesencriptada = new String(b_palabraDesencriptada);
		System.out.println("s_palabraDesencriptada: NADA:" + s_palabraDesencriptada);

		s_palabraDesencriptada = new String(b_palabraDesencriptada, "CP850");
		System.out.println("s_palabraDesencriptada: CP850:" + s_palabraDesencriptada);

		s_palabraDesencriptada = new String(b_palabraDesencriptada, "ISO-8859-1");
		System.out.println("s_palabraDesencriptada: iso-8859-1:" + s_palabraDesencriptada);
	}// testDesencriptarClave

	private static void testEncriptarClave() {
		String key = "MPBKeyED";
		String id = "3434995";
		String claveEncriptar = "Accusys789*";

		encriptarClave(key, id, claveEncriptar);
	}// testEncriptarClave

	private static void encriptarClave(String key, String id, String claveEncriptar) {
		try {
			System.out.println("claveEncriptar:" + claveEncriptar);

			byte[] b_claveEncriptar = claveEncriptar.getBytes("UTF-8");
			String sb_claveEncriptar = Utils.byte2hex(b_claveEncriptar);
			byte[] b_claveEncriptada = Utils.xorstr(key, id, sb_claveEncriptar);
			String claveEncriptada = Utils.byte2hex(b_claveEncriptada);
			System.out.println("claveEncriptada:" + claveEncriptada);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}// pruebaValidacion

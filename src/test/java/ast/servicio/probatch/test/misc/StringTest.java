package ast.servicio.probatch.test.misc;

//import junit.framework.TestCase;

public class StringTest /* extends TestCase */{
	public void testCheckChars() {
		char char13 = (char) 13;
		char char10 = (char) 10;

		System.out.println("\r".charAt(0) == 13);
		System.out.println("\n".charAt(0) == 10);
	}

	public void testReplaceAll() {
		String s = "hola\n como \nandas\ntodo \n bien";
		System.out.println(s);

		s=s.replace("\n", "").replace("\r", "");
		System.out.println(s);
	}// testReplaceAll
}

package ast.servicio.probatch.test;

import java.util.regex.Pattern;

public class PatternTest {

	
	
	
	
	public static void main(String[] args) {

		String path = args[0] + " BUILTIN\\Administradores";
		System.out.println(path);
		String diff = args[0].toUpperCase();
		System.out.println(diff);
		String replaceFirst = path.replaceFirst("(?i)"+Pattern.quote(diff), "abc");
		System.out.println(replaceFirst);
		
		int idx = path.lastIndexOf("\\");
		path = new StringBuilder(path).replace(idx, idx+1, "").toString();

	}

}

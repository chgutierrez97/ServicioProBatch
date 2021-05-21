package ast.servicio.probatch.test;

public class HPUXString {

	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder();
		sb.append("$(");
		sb.append("cd");
		sb.append(")");
		System.out.println(sb.toString());
	}

}

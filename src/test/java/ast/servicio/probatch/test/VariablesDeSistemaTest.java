package ast.servicio.probatch.test;

public class VariablesDeSistemaTest {
	
	public static void main (String[] args) {
		String mensajeC = "---0%---";
		String sisVar = mensajeC.substring(mensajeC.indexOf('%') + 1, mensajeC.substring(mensajeC.indexOf('%') + 1).indexOf('%') + mensajeC.indexOf('%') + 1);
		System.out.println(sisVar);
	}

}

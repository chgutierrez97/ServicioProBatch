package ast.servicio.probatch.util;

import java.util.HashMap;
import java.util.Map;

public class TraductorStatus {

	private static Map<String, String> listStatus = new HashMap<String, String>();

	static {

		listStatus.put("a", "RUNNING");
		listStatus.put("r", "RUNNING");
		listStatus.put("s", "SLEEPING");
		listStatus.put("t", "STOPPED");
		listStatus.put("z", "EXITTING");
		listStatus.put("p", "PAGEWAIT");
		listStatus.put("i", "INTERMEDIATE");
		listStatus.put("d", "DEAD");

	}

	public static String traduce(String status) {
		return listStatus.get(status.toLowerCase());
	}

}

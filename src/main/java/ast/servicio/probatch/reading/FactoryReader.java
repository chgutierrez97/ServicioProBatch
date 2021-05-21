package ast.servicio.probatch.reading;

import java.util.HashMap;
import java.util.Map;

public class FactoryReader {
	
	private static Map <String , IReader> readers = new HashMap<String , IReader>();
	
	static {
		readers.put("0", new BytesReader());
		readers.put("1", new CharacterReader());
	}
	
	public static IReader getReader(String type){
		return readers.get(type);
		
	}

}

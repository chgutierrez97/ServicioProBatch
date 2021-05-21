package ast.servicio.probatch.test.misc;

import java.util.HashMap;
import java.util.Map;

import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.util.ToJsonParser;
//import junit.framework.TestCase;

public class ToJsonParserTest /*extends TestCase */{
	ToJsonParser jsonParser = new ToJsonParser();

	public void testParseMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		int mapSize = 5;
		for (int i = 0; i < mapSize; i++) {
			map.put("key_" + i, "value_" + i);
		}

		String s = jsonParser.parse(map);
		System.out.println(s);
	}
	
	public void testParseObject(){
		Person person = new Person();
		person.setId(99);
		
		System.out.println(jsonParser.parse(person));
	}
	
	
	public void testParseEstadoProceso(){
		String id = "1234";
		String nombre = "MI_PROCESO";
		long ts = 987654;
		int pid= 8798;
		Integer estado = 1;
		EstadoProceso estadoProceso = new EstadoProceso(id , nombre , ts , pid, estado );
		
		System.out.println(jsonParser.parse(estadoProceso));
	}
}

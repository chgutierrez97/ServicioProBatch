package ast.servicio.probatch.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ToJsonParser {
	public String parse(Object obj) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		Class<? extends Object> c = obj.getClass();
		Field[] declaredFields = c.getDeclaredFields();
		for (Field field : declaredFields) {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			String name = field.getName();
			Object value = "";
			try {
				value = field.get(obj);
			} catch (Exception e) {
			}
			
			field.setAccessible(accessible);
			
			map.put(name, value);
		}

		return parse(map);
	}

	public String parse(Map<String, Object> m) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{");

		Set<String> keySet = m.keySet();
		int last = keySet.size() - 1;
		int i = 0;
		for (String key : keySet) {
			Object value = m.get(key);
			value = value == null ? "" : value;

			i++;
			stringBuilder.append(key + ":" + value.toString());
			stringBuilder.append(", ");
		}

		stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		stringBuilder.append("}");

		return stringBuilder.toString();
	}
}// ToJsonParser

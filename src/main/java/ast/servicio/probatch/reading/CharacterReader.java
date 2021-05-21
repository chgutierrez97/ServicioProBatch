package ast.servicio.probatch.reading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CharacterReader implements IReader {

	public String readMessage(InputStream inputStream)  throws IOException {
		String strMensaje = null;
		BufferedReader buffer_in = new BufferedReader(new InputStreamReader(inputStream));
		strMensaje = buffer_in.readLine();
		
		return strMensaje;
	}

}

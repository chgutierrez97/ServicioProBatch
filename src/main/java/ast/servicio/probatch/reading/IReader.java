package ast.servicio.probatch.reading;

import java.io.IOException;
import java.io.InputStream;

public interface IReader {
		
	public String readMessage(InputStream inputStream)  throws IOException;

}

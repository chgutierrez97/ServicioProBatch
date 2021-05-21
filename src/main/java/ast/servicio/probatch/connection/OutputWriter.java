package ast.servicio.probatch.connection;

import java.io.IOException;
import java.io.OutputStream;

public class OutputWriter extends OutputStream {

	private OutputStream writer;

	public OutputWriter(OutputStream writer) {
		this.writer = writer;
	}

	@Override
	public void write(byte[] mensaje) throws IOException {
		// this.writer.flush();
		// String mensajeString = new String(mensaje);
		// if (mensajeString.length() > maxOutputSize) {
		// this.writer.write((mensajeString.substring(0, maxOutputSize) +
		// "\n").getBytes());
		// }else{
//		LoggerFactory.getLogger(getClass()).debug("OutputWriter::write::writer=" + this.writer.toString());
		this.writer.write((new String(mensaje) + "\n").getBytes());
		// }
	}

	@Override
	public void write(int arg0) throws IOException {
		this.writer.write(arg0);
	}

}

package ast.servicio.probatch.reading;

import java.io.IOException;
import java.io.InputStream;

import ast.servicio.probatch.service.ServicioAgente;

public class BytesReader implements IReader {

    public String readMessage(InputStream inputStream) throws IOException {

        int output_maxsize = ServicioAgente.cfg.getOutput_maxsize();

        byte[] msjEntrada = new byte[output_maxsize];
        int readByteCount = 0;

        readByteCount = inputStream.read(msjEntrada);

        if (endOfStream(readByteCount)) {
            String errMsg = "Cantidad de bytes leidos: -1 :: EOF encontrado.";
            ServicioAgente.logger.error(errMsg);
            throw new IOException(errMsg);
        }

        ServicioAgente.logger.debug("bytes_leidos/espacio_buffer: {}/{}", readByteCount, output_maxsize);

		/* se obtiene un string del mensaje... */
        String strMensaje = new String(msjEntrada, 0, readByteCount, "UTF-8").trim();
        // String strMensaje = new String(msjEntrada, "UTF-8").trim();

        return strMensaje;
    }

    /*
     * If the length of b is zero, then no bytes are read and 0 is returned;
     * otherwise, there is an attempt to read at least one byte. If no byte is
     * available because the stream is at end of file, the value -1 is returned;
     * otherwise, at least one byte is read and stored
     */
    private boolean endOfStream(int readByteCount) {
        return readByteCount == -1;
    }
}

package ast.servicio.probatch.connection;

import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.message.Mensaje;
import ast.servicio.probatch.message.MensajeAutenticacion;
import ast.servicio.probatch.reading.FactoryReader;
import ast.servicio.probatch.reading.IReader;
import ast.servicio.probatch.service.ServicioAgente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Conexion {

    public static Logger logger = LoggerFactory.getLogger(Conexion.class);

    private boolean validarRecibido;

    public boolean isValidarRecibido() {
        return validarRecibido;
    }

    public static ServerSocket obtenerServerSocket(int puerto) {
        ServerSocket ssSocket = null;
        try {
            ssSocket = new ServerSocket(puerto);
            System.out.println("Puerto " + puerto + " abierto para escuchar conexiones.");
            //			logger.info("Puerto " + puerto + " abierto para escuchar conexiones.");
        } catch (IOException e) {
            System.err.println("Error al abrir puerto: " + puerto);
            logger.error("Error al abrir puerto: " + puerto);
            logger.trace(e.getMessage());
            System.exit(1);
        }
        return ssSocket;
    }

    public static Socket obtenerConexion(ServerSocket ssSocket) throws IOException {
        System.out.println("Esperando al cliente.");
        Socket sConexion = ssSocket.accept();
        System.out.println("Peticion recibida desde: " + sConexion.getRemoteSocketAddress());
        logger.info("El cliente se conecto desde " + sConexion.getRemoteSocketAddress());
        return sConexion;
    }

    public boolean autenticarCliente(InputStream isEntrada, OutputStream osSalida) throws IOException {
        // Intercambio mensajes para asegurarme que el cliente esta conectado.
        try {
            Mensaje autenticador = obtenerAutenticacionCliente(isEntrada, osSalida);

            if (ServicioAgente.cfg.ignoreAuthentication()) {
                return true;
            }

            MensajeAutenticacion mensajeAutenticacion = (MensajeAutenticacion) autenticador;
            if (mensajeAutenticacion.validarAutenticacion()) {
                System.out.println("Conexion establecida con el cliente.");
                logger.info("Autenticacion exitosa.");
                return true;
            }

        } catch (MensajeErrorException e) {
            System.out.println("Error al autenticar " + e.getRespuestaError());
            logger.error("Error al autenticar " + e.getRespuestaError());
            logger.trace(e.getMessage());
            osSalida.write(e.getRespuestaError().toString().getBytes());
            osSalida.flush();
        }
        return false;
    }

    private Mensaje obtenerAutenticacionCliente(InputStream isEntrada, OutputStream osSalida) {
        Mensaje autConfirm = enviarMensajeConfirmacion(osSalida);
        try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String mensajeConfirmacion = recibirMensajeConfirmacion(isEntrada);

        logger.debug("mensajeConfirmacion antes del trim: {}", mensajeConfirmacion);
        validarRecibido = mensajeConfirmacion.contains("<validar>");

        mensajeConfirmacion = mensajeConfirmacion.replaceAll("<validar></validar>", "").trim();
        // System.out.println("Mensaje de confirmacion con replace : " +
        // mensajeConfirmacion);
        logger.debug("CLI <-- " + autConfirm.getTramaString());
        logger.debug("CLI --> " + mensajeConfirmacion.trim());
        autConfirm.setTramaString(mensajeConfirmacion);
        return autConfirm;
    }

    private Mensaje enviarMensajeConfirmacion(OutputStream osSalida) {
        Mensaje confirmarAutenticacion = null;
        try {
            confirmarAutenticacion = MessageFactory.crearMensajeConfirmacionAutenticacion();
            System.out.println(confirmarAutenticacion.getTramaString());
            osSalida.write(confirmarAutenticacion.getTramaString().getBytes());
            osSalida.flush();
        } catch (Exception e) {
            System.err.println("No se pudo enviar mensaje para autenticar al cliente.");
            logger.error("No se pudo enviar mensaje para autenticar al cliente.");
            logger.trace(e.getMessage());
            ServicioAgente.connectionStatus = false;
        }
        return confirmarAutenticacion;
    }

    private String recibirMensajeConfirmacion(InputStream isEntrada) {
        String strMensaje = "";
        String type = ServicioAgente.cfg.getReaderType();
        IReader reader = FactoryReader.getReader(type);

        try {
            strMensaje = reader.readMessage(isEntrada);
        } catch (Exception e) {
            // Logs
            System.err.println("Se perdio la comunicacion con el cliente al autenticar.");
            logger.error("Se perdio la comunicacion con el cliente al autenticar.");
            logger.trace(e.getMessage());
            ServicioAgente.connectionStatus = false;
        }

        return strMensaje.trim();
    }

    public static void cerrarStreams(Socket socket) {
        try {
            InputStream isEntrada = socket.getInputStream();
            OutputStream osSalida = socket.getOutputStream();
            if (osSalida != null) {
                osSalida.flush();
                osSalida.close();
            }
            if (isEntrada != null) {
                isEntrada.close();
            }
            socket.close();

        } catch (IOException e1) {
            // Logs
            // System.err.println("Error al cerrar los canales de comunicacion.");
            logger.error("Error al cerrar los canales de comunicacion.");
            logger.trace(e1.getMessage());
            System.exit(2);
        }
    }

}

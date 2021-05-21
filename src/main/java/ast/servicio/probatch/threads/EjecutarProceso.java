package ast.servicio.probatch.threads;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.message.Mensaje;
import ast.servicio.probatch.message.MensajeProceso;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.LoggerUtils;
import ast.servicio.probatch.util.Utils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Escucha stdout y stderr de un proceso y vigila que el mismo siga con vida.
 */
public class EjecutarProceso extends Thread {

    public static final int ERROR_FATAL = -9999;
    public static Logger logger = LoggerFactory.getLogger(EjecutarProceso.class);
    public static Logger loggerProceso;
    protected OutputStream osSalida;
    protected ParametrosProceso parametroP;
    protected Process process;
    public static boolean procesoTerminado;

    public EjecutarProceso(Process process, OutputStream osSalidaSocket, ParametrosProceso parametroP) {
        super("Ejecucion Proceso (" + parametroP.getId() + ")");
        this.process = process;
        this.osSalida = osSalidaSocket;
        this.parametroP = parametroP;
        loggerProceso = LoggerUtils.createLoggerProceso(parametroP);
        procesoTerminado = false;

    }

    @Override
    public void run() {

        try {
            MensajeProceso.terminarThreadsLocal = false;
            loggerProceso.info(" *** " + parametroP.getComando() + " *** ");
            // List<Listener> listeners = new ArrayList<Listener>();
            // Listener procesoInput;
            // Listener procesoError;

            List<? extends Listener> listeners = Collections.unmodifiableList(ListenerFactory.getListeners(this.process, this.osSalida, this.parametroP));

            // if (OsServiceFactory.isAS400SO()) {
            // procesoInput = new ListenerProcesoAS400(process, osSalida,
            // parametroP);
            // procesoInput.setDaemon(true);
            // procesoInput.start();
            // listeners.add(procesoInput);
            // } else {
            //
            // procesoInput = new ListenerProceso(ListenerProceso.TYPE_INPUT,
            // process, osSalida, parametroP);
            // procesoInput.setDaemon(true);
            // procesoInput.start();
            // listeners.add(procesoInput);
            //
            // procesoError = new ListenerProceso(ListenerProceso.TYPE_ERROR,
            // process, osSalida, parametroP);
            // procesoError.setDaemon(true);
            // procesoError.start();
            // listeners.add(procesoError);
            // }

            String mensajeTransicion = "";

            try {

                int exitVal = 0;
                /*
                 * se verifica si el proceso corriendo fue matado
				 * ---------------------------------------------------------
				 */
                boolean procesoFueMatado = parametroP.procesoFueMatado();
                if (procesoFueMatado) {
                    logger.debug("Proceso " + parametroP.getId() + " figura como matado");
                    joinThreads(listeners);
                    // joinThread(procesoInput, 1000, "Listener de stdout de
                    // proceso " + parametroP.getId() + " fue interrumpido");
                    // joinThread(procesoError, 1000, "Listener de stderr de
                    // proceso " + parametroP.getId() + " fue interrumpido");
                    return;
                }
                /*----------------------------------------------------------------------------------------------------------*/

                boolean muerteIntencional = false;

				/*
                 * AQUI SE OBTIENE EL EXIT CODE DEL PROCESO LANZADO
				 */

                logger.debug("Proceso " + parametroP.getId() + " llama a process.waitFor");
                exitVal = process.waitFor();
                Thread.sleep(1000);
                logger.debug("process.waitFor() de Proceso " + parametroP.getId() + "=" + exitVal);

                logger.debug("Esperando a que los listeners de proceso de " + parametroP.getId() + " terminen...");
                // joinThread(procesoInput, 1000, "Listener de stdout de proceso
                // " + parametroP.getId() + " fue interrumpido");
                // joinThread(procesoError, 1000, "Listener de stderr de proceso
                // " + parametroP.getId() + " fue interrumpido");
                joinThreads(listeners);

                logger.debug("Los listeners de proceso de " + parametroP.getId() + " terminaron");

                // Valido si hubo errores al terminar
                // if (exitVal == 0 && procesoInput.getErrorFatal() == null) {
                if (exitVal == 0 && listenersOK(listeners)) {

                    if (validacionInterfaces(parametroP.getInterfaces(), "salida") != null) {

                        mensajeTransicion = validacionInterfaces(parametroP.getInterfaces(), "salida").getTramaString();
                        logger.debug(mensajeTransicion);
                        logger.error("No se generaron las interfaces de salida para el proceso de id " + parametroP.getId() + " con estado = " + exitVal);

                    } else if (parametroP.getResultado() != null && validacionResultado(parametroP.getResultado()) != null) {

                        mensajeTransicion = validacionResultado(parametroP.getResultado()).getTramaString();
                        logger.debug(mensajeTransicion);
                        logger.error("No se genero la interfaz de resultado para el proceso de id " + parametroP.getId() + " con estado = " + exitVal);

                    } else {
                        // Proceso finalizo correctamente
                        if (parametroP.getResultado() != null && !parametroP.getResultado().getValor().equals("")) {
                            List<String> listaResultado = leerBytesArchivo(parametroP.getResultado().getValor(), ServicioAgente.cfg.getResult_maxsize());

                            for (String mensajeResutlado : listaResultado) {
                                mensajeTransicion = "<mensaje id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\""
                                        + Mensaje.calcularTS() + "\">" + mensajeResutlado + "</mensaje>";
                                osSalida.write(mensajeTransicion.getBytes());
                                logger.debug(mensajeTransicion);
                                loggerProceso.info(mensajeTransicion);
                            }
                        }

                        mensajeTransicion = String.format("<transicion id=\"%s\" nombre=\"%s\" ts=\"%d\">%s</transicion>", parametroP.getId(),
                                parametroP.getNombre(), Mensaje.calcularTS(), traducirEstado(exitVal));

                        logger.debug("CLI <-- " + mensajeTransicion);
                        logger.info("Termino el proceso {} con status {}", parametroP.getId(), exitVal);
                        loggerProceso.info("Termino el proceso {} con status ", parametroP.getId(), exitVal);
                    }

                    // else if (exitVal == 0 && procesoInput.getErrorFatal()  != null) {
                } else if (exitVal == 0 && !listenersOK(listeners)) {
                    // SI LOS LISTENERS TIENEN ERRORES
                    logger.debug("ERROR FATAL DETECTADO EN PROCESO" + parametroP.getPid());

                    exitVal = ERROR_FATAL;
                    // loggerProceso.info("Termino el proceso de id " +
                    // parametroP.getId() + " con error fatal : " +
                    // procesoInput.getErrorFatal());
                    mensajeTransicion = "<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + Mensaje.calcularTS()
                            + "\">" + traducirEstado(exitVal) + "</transicion>";

                    logger.debug(mensajeTransicion);
                } else if (exitVal != 0) {
                    // SI EL PROCESO TERMINO CON ESTADO DISTINTO DE CERO
                    mensajeTransicion = "<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + Mensaje.calcularTS()
                            + "\">" + traducirEstado(exitVal) + "</transicion>";

                    logger.debug("CLI <-- " + mensajeTransicion);
                    logger.info("Termino el proceso de id #" + parametroP.getId() + " con status " + exitVal);
                    osSalida.write(mensajeTransicion.getBytes());

                } else if (!validarMuerteIntencional(parametroP)) {

                    mensajeTransicion = "<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + parametroP.getTs()
                            + "\">" + traducirEstado(exitVal) + "</transicion>";

                    logger.info("Termino el proceso de id " + parametroP.getId() + " con status " + exitVal);

                    logger.debug("CLI <-- " + mensajeTransicion);
                    MensajeProceso.actualizarEstadoProceso(parametroP, exitVal);
                    osSalida.write(mensajeTransicion.getBytes());
                } else {
                    muerteIntencional = true;
                }

                // Solo se actualiza estado si no fue muerte intencional, ya
                // que en este ultimo caso se actualiza
                // en la operacion MensajeMatar
                if (!muerteIntencional) {
                    MensajeProceso.actualizarEstadoProceso(parametroP, exitVal);
                    byte[] toSend = (mensajeTransicion == null || mensajeTransicion.contentEquals("")) ? null : mensajeTransicion.getBytes();
                    osSalida.write(toSend);
                    osSalida.flush();
                }
                Thread.sleep(1000);

            } catch (java.net.SocketException se) {
                logger.error("Ocurrio un error de conexion durante la ejecucion de " + parametroP.getComando() + "::" + se.getMessage());

            } catch (FileNotFoundException e) {
                logger.error("Error al leer la interfaz resultado " + e.getMessage());
                logger.trace(e.getMessage());
            } catch (Exception e) {

                if (process.exitValue() != 1) { logger.error(e.getMessage()); }
            }

        } catch (Exception e) {
            logger.error("Error al escribir en socket: " + e.getMessage());
            logger.trace(e.getMessage());
        }
    }

    /**
     * Obtiene el mensaje del errorFatal encontrado en los listeners, vacio en caso que no haya.
     *
     * @param listeners
     * @return mensaje
     */
    private String getFatalErrorMsg(List listeners) {
        for (Object object : listeners) {
            Listener listener = (Listener) object;
            if (listener.getErrorFatal() != null) {
                return listener.getErrorFatal();
            }
        }
        return "";
    }

    /**
     * Verificar si hay errores fatales en los listeners.
     *
     * @param listeners Listeners activos de proceso
     * @return true si los listeners estan OK, false si tienen errores.
     */
    private boolean listenersOK(List<? extends Listener> listeners) {
        for (Listener listener : listeners) {
            if (listener.getErrorFatal() != null) {
                logger.debug("Ocurrio error fatal en Listener " + listener.getType() + ", error: " + listener.getErrorFatal());
                return false;
            }
        }
        logger.debug("Listeners OK");
        return true;
    }

    private void joinThreads(List<? extends Listener> listeners) {
        for (Listener listener : listeners) {
            logger.debug("Terminando listener " + listener.getType());
            joinThread(listener, 1000, "Listener de proceso " + parametroP.getId() + " fue interrumpido");
        }

    }

    /**
     * Valida si el fin del proceso se dio por una muerte intencional a traves
     * del mensaje matar. En ese caso no registra ningun mensa en la salida ni
     * en logs ya que de eso se encarga el mensaje matar
     *
     * @param param
     * @return
     */
    protected boolean validarMuerteIntencional(ParametrosProceso param) {

        logger.debug("EjecutarProceso solicita lista estado mensajes...");
        List<EstadoProceso> listaEstados = ServicioAgente.getEstadoMensajes();
        String idProceso = param.getId();
        synchronized (listaEstados) {
            for (EstadoProceso estadoProceso : listaEstados) {
                if (estadoProceso.getId().equals(idProceso)) {
                    if (estadoProceso.getEstado() != null && estadoProceso.getEstado() == ERROR_FATAL) {
                        logger.debug("validarMuerteIntencional::MUERTE INTENCIONAL DETECTADA PARA PROCESO" + estadoProceso.toString() + "!");
                        return true;
                    }
                }

            }
        }
        return false;
    }

    private Mensaje validacionInterfaces(Collection<Atributo> interfaces, String tipo) {

        String interfaz = null;
        String noEcontrados = null;
        String controlarTodos = null;

        for (Iterator<Atributo> iterator = interfaces.iterator(); iterator.hasNext(); ) {
            Atributo atributo = iterator.next();
            if (tipo.equalsIgnoreCase(atributo.getNombre())) {
                interfaz = atributo.getValor();
                controlarTodos = atributo.getControlar_todos();

            }
        }
        if (interfaz != null) {

            noEcontrados = Utils.archivosNoEncontrados(interfaz);

            if (!noEcontrados.equals("")) {

                if (controlarTodos.equals("1")) {
                    return MessageFactory.crearMensajeRespuesta("<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\""
                            + Mensaje.calcularTS() + "\"><fin estado=\"falla\" valor=\"No se puede leer el archivo " + StringEscapeUtils.escapeXml(noEcontrados)
                            + ": No such file or directory\"/></transicion>");
                } else if (controlarTodos.equals("0") && noEcontrados.equals(interfaz)) {
                    return MessageFactory.crearMensajeRespuesta("<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\""
                            + Mensaje.calcularTS() + "\"><fin estado=\"falla\" valor=\"No se puede leer el archivo " + StringEscapeUtils.escapeXml(noEcontrados)
                            + ": No such file or directory\"/></transicion>");
                }

            }
        }
        return null;

    }

    private Mensaje validacionResultado(Atributo resultado) {

        String interfaz = resultado.getValor();
        String noEcontrados = null;

        if (interfaz != null) {

            noEcontrados = Utils.archivosNoEncontrados(interfaz);

            if (!noEcontrados.equals("")) {

                return MessageFactory.crearMensajeRespuesta(
                        "<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + Mensaje.calcularTS()
                                + "\"><fin estado=\"falla\" valor=\"0\" interfaces=" + StringEscapeUtils.escapeXml(noEcontrados) + "/></transicion>");

            }
        }
        return null;

    }

    private static List<String> leerBytesArchivo(String fileName, int kBytesALeer) throws IOException {
        List<String> listaBytesLeidos = new ArrayList<String>();
        int bytesALeer = kBytesALeer * 1024;
        int tamanoArchivo;
        int resto;
        String ultimoStringLista;
        String ultimoStringListaModificado;

        FileInputStream fileInput = new FileInputStream(fileName);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInput);
        tamanoArchivo = bufferedInputStream.available();
        if (tamanoArchivo > 0) {
            resto = tamanoArchivo % bytesALeer;
            byte[] bytes = new byte[bytesALeer];

            while (bufferedInputStream.read(bytes) != -1) {
                listaBytesLeidos.add(new String(bytes));
            }

            ultimoStringLista = listaBytesLeidos.get(listaBytesLeidos.size() - 1);
            ultimoStringListaModificado = ultimoStringLista.substring(0, resto);

            listaBytesLeidos.set(listaBytesLeidos.size() - 1, ultimoStringListaModificado);
        }
        bufferedInputStream.close();
        return listaBytesLeidos;
    }

    private String traducirEstado(int estado) {
        logger.debug("TRADUCIENDO ESTADO " + estado + " DE PROCESO " + parametroP.getId());
        switch (estado) {
            case 0:
                return "<fin estado=\"exito\" />";
            case ERROR_FATAL:
                return "<fin estado=\"muerte\" />";
            default:
                return String.format("<fin estado=\"falla\" valor=\"%d\"/>", estado);
        }
    }

    private static void joinThread(Thread t, int time, String interruptMsg) {
        try {
            t.join(time);
        } catch (InterruptedException e) {
            logger.debug(interruptMsg);
        }
    }
}
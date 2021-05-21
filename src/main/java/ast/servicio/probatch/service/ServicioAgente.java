package ast.servicio.probatch.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.configuration.Configurador;
import ast.servicio.probatch.connection.Conexion;
import ast.servicio.probatch.connection.OutputWriter;
import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.reading.FactoryReader;
import ast.servicio.probatch.reading.IReader;
import ast.servicio.probatch.threads.Heartbeat;
import ast.servicio.probatch.threads.ServiceCleanDump;
import ast.servicio.probatch.threads.ServiceCleanLogs;
import ast.servicio.probatch.threads.ServiceDump;
import ast.servicio.probatch.threads.ServiceProcessMessage;
import ast.servicio.probatch.util.FileUtils;
import ast.servicio.probatch.util.ToJsonParser;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class ServicioAgente {
    public final static String VERSION = "5.7.2";
    final String CATEGORIA = "batch";
    public static Configurador cfg;
    private static List<EstadoProceso> estadoMensajes;
    public static String winExecAs;
    public static boolean connectionStatus = false;
    public static boolean terminarThreads;
    private static String osName = System.getProperty("os.name");

    public static Logger logger;

    public static boolean printVersion(String[] args) {
        if (args == null || args.length == 0) {
            return false;
        }

        String arg = args[0].toLowerCase();
        if (arg.contentEquals("-v") || arg.contentEquals("--version")) {
            return true;
        }

        return false;
    }// printVersion

    public static void main(String[] args) throws IOException {
        // logger.debug("Cantidad argumentos del array main: " + args.length);
        if (printVersion(args)) {
            System.out.println("Servicio Agente Probatch V" + VERSION);
            return;
        }

        logger = LoggerFactory.getLogger(ServicioAgente.class);
        String archCfg;

        if (args == null || args.length < 1) {
            archCfg = "mantis.conf";
        } else {
            archCfg = args[0];
        }

        initLogger(archCfg);

        System.out.println("Archivo de Configuracion: " + archCfg);
        ServicioAgente probatch = new ServicioAgente(archCfg);
        // logger.debug(osName);

        if (osName.contains("Xp") || osName.contains("2003")) {
            // winExecAs = cfg.getWinRunAs();
            winExecAs = cfg.getWinScript(cfg.getWinRunAs());
        } else {
            // winExecAs = cfg.getRunAsUser();
            winExecAs = cfg.getWinScript(cfg.getRunAsUser());
        }

		/* hago un cleanup inicial de archivos temporales... */
        runPidFilesCleanUp();

        probatch.ejecutarServicio();

		/* hago un cleanup final de archivos temporales... */
        runPidFilesCleanUp();

    }

    private static void runPidFilesCleanUp() {
        FileUtils fileUtils = FileUtils.getInstance();
        File dir = new File("./");
        String regexp = "^pid_.*";
        fileUtils.deleteFiles(dir, regexp);
    }

    private static void initLogger(String fileConfig) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(context);
        context.reset(); // override default configuration
        // inject the name of the current application as "application-name"
        // property of the LoggerContext
        context.putProperty("PROBATCH-CONFIG", fileConfig);
        try {
            File file = new File("ServicioAgente.jar");
            JarFile jarFile;
            jarFile = new JarFile(file);

            InputStream is = jarFile.getInputStream(jarFile.getEntry("logback.xml"));

            jc.doConfigure(is);
        } catch (JoranException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ServicioAgente(String archCfg) {
        cfg = new Configurador(archCfg);

        logger.debug("DEBUG MODE=" + cfg.getDebugMode());

        // Declara los tipos de mensajes validos.
        // estadoMensajes = Utils.levantarEstados(cfg.getWrkdir() + "/" +
        // cfg.getDump_file());
        estadoMensajes = Collections.synchronizedList(new LinkedList<EstadoProceso>());
    }

    public void ejecutarServicio() throws IOException {
        ToJsonParser toJsonParser = new ToJsonParser();
        logger.debug("CONFIGURACION=" + toJsonParser.parse(ServicioAgente.cfg));

        // logger.info("Iniciando Servicio");

        logger.info("probatch version " + VERSION);
        logger.info("Escuchando en el puerto " + cfg.getPort());
        Socket socket = null;
        ServerSocket serverSocket = null;

        String type = ServicioAgente.cfg.getReaderType();
        IReader reader = FactoryReader.getReader(type);

        while (true) {
            // Establece una unica conexion con un unico cliente.
            try {

                serverSocket = Conexion.obtenerServerSocket(cfg.getPort());
                terminarThreads = false;
                socket = Conexion.obtenerConexion(serverSocket);
                socket.setKeepAlive(true);
                // socket.setSoTimeout(cfg.getTimeout_socket());
                InputStream entrada = socket.getInputStream();

                OutputWriter output = new OutputWriter(socket.getOutputStream());

                // Thread de latido
                Heartbeat hearbeat = new Heartbeat(ServicioAgente.cfg.getHeartbeat_interval() * 1000, output);
                // Thread de resguardo de estados
                ServiceDump serviceDump = new ServiceDump(ServicioAgente.cfg.getDump_interval() * 1000, ServicioAgente.cfg.getWrkdir(),
                        ServicioAgente.cfg.getDump_file());
                // Thread para limpiar archivo de estados
                double timeoutCleanLogs = ServicioAgente.cfg.getTimeout();
                ServiceCleanDump serviceCleanDump = new ServiceCleanDump((long) (timeoutCleanLogs * 3600000), ServicioAgente.cfg.getWrkdir(),
                        ServicioAgente.cfg.getDump_file());
                // Thread para limpiar logs.
                ServiceCleanLogs cleanLogs = new ServiceCleanLogs(ServicioAgente.cfg.getClean_logs() * 1000, ServicioAgente.cfg.getWrkdir(), CATEGORIA);

                Conexion conexion = new Conexion();
                if (conexion.autenticarCliente(entrada, output)) {
                    ServicioAgente.connectionStatus = true;
                    // Inicio el thread de Latido.
                    hearbeat.start();
                    // Inicio el thread de ServiceDump.
                    serviceDump.start();
                    // Inicio el thread de ServiceCleanDump.
                    serviceCleanDump.start();
                    // Inicio el thread de CleanLogs.
                    cleanLogs.start();

                    if (conexion.isValidarRecibido()) {
                        logger.info("validar recibido en mensaje de autenticacion");
                        ServiceProcessMessage serviceCut = new ServiceProcessMessage(entrada, output, "<validar></validar>");
                        serviceCut.start();
                    }

                    while (ServicioAgente.connectionStatus) {
                        try {
                            output.flush();
                            String strMensaje = reader.readMessage(entrada);
                            if (strMensaje == null) {
                                ServicioAgente.connectionStatus = false;
                            }
                            logger.debug("CLI --> " + strMensaje);
                            /* se procesa el mensaje en un thread nuevo */
                            ServiceProcessMessage serviceCut = new ServiceProcessMessage(entrada, output, strMensaje);
                            serviceCut.start();

                        } catch (IOException e) {
                            // Logs
                            logger.error("El cliente cerro la conexion");
                            logger.error(e.getMessage());
                            ServicioAgente.connectionStatus = false;
                        } catch (Exception e) {
                            // Logs
                            logger.error(e.getMessage());
                            ServicioAgente.connectionStatus = false;
                        }
                    }
                }
                // Termino los threads
                terminarThreads = true;
                logger.info("terminarThreads...");
                terminarThreads(hearbeat, serviceDump, cleanLogs, serviceCleanDump);
                // Cuando pierdo la conexion cierro los streams de entrada y
                // salida,
                // para permitir una nueva conexion.

            } catch (Exception e) {
                logger.trace(e.getMessage());
                logger.error("Error en la conexion");
                System.err.print("Error en la conexion");
            } finally {
                logger.debug("Cerrando STREAMS!");

                if (socket != null) {
                    Conexion.cerrarStreams(socket);
                }
                if (serverSocket != null) {
                    serverSocket.close();
                }
                terminarThreads = true;
            }

        }

    }

    /**
     * Termina los threads para reiniciar el servicio.
     *
     * @param hearbeat
     * @param serviceDump
     * @param cleanLogs
     * @param serviceCleanDump
     */
    private void terminarThreads(Heartbeat hearbeat, ServiceDump serviceDump, ServiceCleanLogs cleanLogs, ServiceCleanDump serviceCleanDump) {
        try {
            hearbeat.interrupt();
            serviceDump.interrupt();
            cleanLogs.interrupt();
            serviceCleanDump.interrupt();
            System.out.println("los threads se cerraron");
        } catch (Exception e) {
            logger.trace(e.getMessage());
        }
    }

    public static List<EstadoProceso> getEstadoMensajes() {
        // if (estadoMensajes != null && estadoMensajes.isEmpty() == false) {
        // logger.debug("CONTENIDO DE LISTA DE ESTADO MENSAJES: " +
        // estadoMensajes.toString());
        // } else {
        // logger.debug("LISTA ESTADO MENSAJES ESTA VACIA!");
        // }
        return estadoMensajes;
    }

    public static void setEstadoMensajes(List<EstadoProceso> estadoMensajes) {
        // String msg =
        // "setEstadoMensajes::SETEANDO LISTA DE ESTADO MENSAJES; NUEVA LISTA=
        // ";
        // if (estadoMensajes == null || estadoMensajes.isEmpty()) {
        // msg += "[]";
        // } else {
        // msg += Arrays.toString(estadoMensajes.toArray());
        // }
        // logger.debug(msg);
        ServicioAgente.estadoMensajes = estadoMensajes;
    }

    public static void borrarListaEstadoMensajes() {
        // logger.debug("borrarListaEstadoMensajes::BORRANDO LISTA DE ESTADO
        // MENSAJES!");
        ServicioAgente.estadoMensajes = Collections.synchronizedList(new LinkedList<EstadoProceso>());
    }

}

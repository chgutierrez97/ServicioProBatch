package ast.servicio.probatch.threads;

import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ServiceDump extends Thread {

    public static Logger logger = LoggerFactory.getLogger(ServiceDump.class);

    private long intervaloBajada;
    private String wkdir;
    private String dumpFileName;
    private FileWriter fileWriter = null;
    private String leido = "";
    private boolean terminarEsteThread;

    public ServiceDump(long intervaloBajada, String wkdir, String dumpFileName) {
        super("ServiceDump Thread");
        this.intervaloBajada = intervaloBajada;
        this.wkdir = wkdir;
        this.dumpFileName = dumpFileName;
    }

    @Override
    public void run() {
        try {
            while (!ServicioAgente.terminarThreads && !terminarEsteThread) {

                //				logger.debug("ServiceDump solicita lista estado mensajes...");
                List<EstadoProceso> estadosSinBajar = estadosNoDump(ServicioAgente.getEstadoMensajes());

                try {
                    File dumpFile = new File(wkdir + "/" + dumpFileName);

                    if (dumpFile.exists()) {
                        FileReader fileReader = new FileReader(dumpFile);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        leido = bufferedReader.readLine();
                        fileReader.close();
                    }

                    List<EstadoProceso> listaEstadosArchivo = Utils.stringEstadosToListaEstados(leido);

                    for (EstadoProceso estadoSinBajar : estadosSinBajar) {
                        boolean flag = false;
                        for (EstadoProceso estadoArchivo : listaEstadosArchivo) {
                            if (estadoSinBajar.getId().equals(estadoArchivo.getId())) {
                                estadoArchivo.setEstado(estadoSinBajar.getEstado());
                                estadoArchivo.setId(estadoSinBajar.getId());
                                estadoArchivo.setNombre(estadoSinBajar.getNombre());
                                estadoArchivo.setPid(estadoSinBajar.getPid());
                                estadoArchivo.setTs(estadoSinBajar.getTs());
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            listaEstadosArchivo.add(estadoSinBajar);
                        }
                        estadoSinBajar.setDump(true);
                    }

                    fileWriter = new FileWriter(wkdir + "/" + dumpFileName);
                    PrintWriter printWriter = new PrintWriter(fileWriter);
                    printWriter.print("<estado-guardado>");

                    for (EstadoProceso estado : listaEstadosArchivo) {
                        printWriter.print(estado.getMensajeTransicionEstado().getTramaString().trim());
                    }
                    printWriter.print("</estado-guardado>");
                    fileWriter.close();
                    logger.info("Dump!");
                    sleep(intervaloBajada);
                } catch (IOException iO) {
                    logger.error("No se puede escribir en archivo" + dumpFileName);
                    logger.trace(iO.getMessage());
                } finally {
                    closeFileWriter();
                }

            }
        } catch (InterruptedException e) {
            terminarEsteThread = true;
        }

    }

    private void closeFileWriter() {
        try {
            if (fileWriter != null) {
                fileWriter.close();
            }
        } catch (IOException iO) {
            logger.error("Error al cerrar stream de archivo dump" + dumpFileName);
            logger.trace(iO.getMessage());
        }
    }

    private List<EstadoProceso> estadosNoDump(List<EstadoProceso> estadoMensajes) {
        List<EstadoProceso> listaResultado = Collections.synchronizedList(new LinkedList<EstadoProceso>());
        for (EstadoProceso estado : estadoMensajes) {
            if (!estado.isDump()) {
                listaResultado.add(estado);
            }
        }
        return listaResultado;
    }
}

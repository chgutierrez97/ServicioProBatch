package ast.servicio.probatch.message;

import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.os.service.OsServiceFactory;
import ast.servicio.probatch.os.service.recursive.FactoryRecursiveKillers;
import ast.servicio.probatch.os.service.recursive.RecursiveKiller;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

public class MensajeMatar extends Mensaje {

    public static Logger logger = LoggerFactory.getLogger(MensajeMatar.class);

    public MensajeMatar(String mensajeEntrada) {
        super(mensajeEntrada);

    }

    public Mensaje procesarMensaje(OutputStream oSalida) {

        logger.debug("MensajeMatar solicita lista estado mensajes...");
        List<EstadoProceso> listaEstados = ServicioAgente.getEstadoMensajes();
        String idProceso = Utils.obtenerParametroTramaString(this.getTramaString(), "id");
        synchronized (listaEstados) {
            for (EstadoProceso estadoProceso : listaEstados) {
                if (estadoProceso.getId().equals(idProceso)) {

                    logger.debug("estadoProceso.getID : " + estadoProceso + " idProceso" + idProceso);

                    if (estadoProceso.getEstado() == null) {
                        logger.debug("procesarMensaje::MATANDO PROCESO " + estadoProceso.toString());
                        // logger.debug("CMD[" + estadoProceso.getId() + "]" +
                        // " matar pid=" + estadoProceso.getPid());
                        matar(estadoProceso.getPid());
                        estadoProceso.setEstado(-9999);
                        return estadoProceso.getMensajeTransicionEstado();
                    } else {
                        return MessageFactory.crearMensajeError(null, "El proceso ya ha terminado con valor" + estadoProceso.getEstado());
                    }
                }
            }
        }

        return MessageFactory.crearMensajeError("No se encontro el proceso id: " + idProceso);

    }

    public static boolean matar(int pid) {
        try {
            /*
			 * Primero se verifica si lo que se va a eliminar es el PID Padre o
			 * un PID de un subproceso
			 */
            logger.debug("MATANDO PROCESO " + pid);

            FactoryRecursiveKillers factory = new FactoryRecursiveKillers();
            RecursiveKiller recursiveKiller = factory.getKiller(OsServiceFactory.checkOs().trim());
            //			logger.info("SISTEMA OPERATIVO: " + OsServiceFactory.checkOs());

            if (recursiveKiller == null) {
                String killCommand = OsServiceFactory.getOsService().getKillCommand(pid);
                Runtime.getRuntime().exec(killCommand);
            } else {
                recursiveKiller.initKiller(pid);
            }
        } catch (Exception e1) {
            logger.debug("Error al eliminar el pid " + pid + " - " + e1);
            return false;
        }
        return true;
    }

}

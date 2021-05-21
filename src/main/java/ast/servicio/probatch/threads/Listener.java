package ast.servicio.probatch.threads;

import ast.servicio.probatch.domain.ParametrosProceso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Listener extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(Listener.class);

    private String errorFatal;
    ParametrosProceso parametroP;
    String type;

    public Listener(String string) {
        super(string);
    }

    public synchronized String getErrorFatal() {
        return errorFatal;
    }

    public synchronized void setErrorFatal(String errorFatal) {
        logger.debug("SETEANDO errorFatal DE PROCESO {}", parametroP.getPid());
        logger.debug("errorFatal: {}", errorFatal);
        this.errorFatal = errorFatal;
    }

    public String getType() {
        return type;
    }
}
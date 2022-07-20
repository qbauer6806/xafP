package mc.gouv.xaf.back.paiement.retry;

import org.slf4j.Logger;

public abstract class Operation {

    abstract public void execute() throws Exception;

    abstract public Logger getLogger();

    public void handleException(Exception exception) {
        getLogger().error(exception.getMessage(), exception);
    }


}

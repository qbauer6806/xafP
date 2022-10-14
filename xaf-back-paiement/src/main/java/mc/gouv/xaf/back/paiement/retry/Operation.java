package mc.gouv.xaf.back.paiement.retry;

import org.slf4j.Logger;

import java.util.Optional;

public abstract class Operation<T> {
    private T result;

    abstract public void execute() throws Exception;

    abstract public Logger getLogger();

    public void handleException(Exception exception) {
        getLogger().error(exception.getMessage(), exception);
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Optional<T> getResult() {
        return Optional.ofNullable(this.result);
    }


}

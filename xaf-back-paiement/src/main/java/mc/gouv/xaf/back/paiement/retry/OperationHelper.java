package mc.gouv.xaf.back.paiement.retry;

import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import org.apache.http.client.HttpResponseException;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class OperationHelper {

    private PaiementPropertiesResolver paiementPropertiesResolver;

    public OperationHelper(PaiementPropertiesResolver paiementPropertiesResolver) {
        this.paiementPropertiesResolver = paiementPropertiesResolver;
    }

    public void executeWithRetry(Operation operation) throws Exception {
        executeWithRetry(operation,
                paiementPropertiesResolver.getXafRetryCount(),
                paiementPropertiesResolver.getXafRetryInitialDelay(),
                paiementPropertiesResolver.getXafRetryMultiplier());
    }

    public void executeWithRetry(Operation operation, int maxAttempts, int delay, int multiplier) throws Exception {
        for (int count = 0; ; count++) {
            operation.getLogger().info("Tentative n°" + (count + 1));
            try {
                operation.execute();
                return;
            } catch (HttpResponseException exception) {
                operation.handleException(exception);
                sleep(operation, delay);
                delay *= multiplier;
                if ((exception.getStatusCode() >= 400 && exception.getStatusCode() <= 499 ) || count >= (maxAttempts - 1)) {
                    throw exception;
                }
            }
        }
    }

    private void sleep(Operation operation, int delay) {
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException exception) {
            operation.getLogger().error("Delai du retry interrompu [" + exception.getMessage() + "]", exception);
        }
    }
}

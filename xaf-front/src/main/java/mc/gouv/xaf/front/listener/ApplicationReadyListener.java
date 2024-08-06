package mc.gouv.xaf.front.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.exception.XafException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationReadyListener.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        JsonNode config;
        try {
            config = xafFrontserverUtils.getConfig();
        } catch (IOException e) {
            LOGGER.error("Impossible de lire le fichier config.json");
            throw new XafException(e);
        }
        // suppression du noeud donneesExternes car pas utile à sauvegarder dans la bdd
        ((ObjectNode) config).remove("donneesExternes");
        AfApiClient afApiClient = xafFrontserverUtils.getAfApiClient();
        while (afApiClient.creerConfig(config) == null) {
            LOGGER.info("API injoignable, nouvel essai dans 10 secondes");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new XafException(e);
            }
        }
        LOGGER.info("API joignable");
    }
}

package mc.gouv.xaf.back.paiement.client.monetico;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import mc.gouv.xaf.back.paiement.client.PaiementClient;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementStatutBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationStatutBO;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.service.ReferenceFactoryService;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.Date;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Component
public class MoneticoClient implements PaiementClient {

    private static Logger LOGGER = LoggerFactory.getLogger(ReferenceFactoryService.class);

    private final WebTarget target;
    private final String tpe;

    private final PaiementPropertiesResolver paiementPropertiesResolver;

    private static SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public MoneticoClient(Proxy proxy, PaiementPropertiesResolver paiementPropertiesResolver) {

        ClientConfig config = new ClientConfig();

        HttpUrlConnectorProvider cp = new HttpUrlConnectorProvider();
        config.connectorProvider(cp);
        cp.connectionFactory(url -> (HttpURLConnection) url.openConnection(proxy));
        config.register(JacksonJsonProvider.class);
        Client client = ClientBuilder.newClient(config);

        this.tpe = paiementPropertiesResolver.getTpe();

        this.target = client.target(paiementPropertiesResolver.getCaptureUrl());
        this.paiementPropertiesResolver = paiementPropertiesResolver;
    }

    @Retryable(value = Exception.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void capture(MoyenPaiementBO paiement, OperationBO operation) throws Exception {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ MoyenPaiementBO {}] ", paiement);
        Date date = new Date(System.currentTimeMillis());
        String dateString = simpleDateFormat.format(date);
        String dateTimeString = simpleDateTimeFormat.format(date);

        Response response = this.target.queryParam("TPE", this.tpe)
                .queryParam("montant", paiement.getMontantInitial() + paiementPropertiesResolver.getCurrency())
                .queryParam("montant_a_capturer", operation.getMontant() + paiementPropertiesResolver.getCurrency())
                .queryParam("montant_deja_capture", paiement.getMontantCapture() + paiementPropertiesResolver.getCurrency())
                .queryParam("montant_restant", (paiement.getMontantRestant() - operation.getMontant()) + paiementPropertiesResolver.getCurrency())
                .queryParam("lgue", "FR")
                .queryParam("reference", paiement.getPkMoyenPaiement())
                .queryParam("date", dateTimeString)
                .queryParam("date_commande", dateString)
                .queryParam("societe", paiement.getCodeSociete())
                .queryParam("version", paiementPropertiesResolver.getVersionCapture())
                .request(MediaType.APPLICATION_JSON).get();

        String responseString = response.readEntity(String.class);
        LOGGER.info("Capture [ responseString {}] ", responseString);

        for (String s : responseString.split("\n")) {
            String[] keyValue = s.split("=");

            switch (keyValue[0]) {
                case "cdr": // cdr = code retour
                    if ("1".equals(keyValue[1])) {
                        operation.setOperationStatut(OperationStatutBO.ACCEPTEE);
                    } else if ("0".equals(keyValue[1])) {
                        operation.setOperationStatut(OperationStatutBO.REFUSEE);
                        paiement.setMoyenPaiementStatut(MoyenPaiementStatutBO.INVALIDE);
                    } else {
                        operation.setOperationStatut(OperationStatutBO.ERREUR);
                    }
                    break;
                case "aut": // aut = numero d'autorisation
                    operation.setNumeroAuthorisation(Integer.parseInt(keyValue[1]));
                    break;
            }
        }


    }

}

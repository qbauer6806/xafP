package mc.gouv.xaf.back.stc.client.monetico;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import mc.gouv.xaf.back.stc.client.PaiementClient;
import mc.gouv.xaf.back.stc.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.stc.service.ReferenceFactoryService;
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

import static mc.gouv.xaf.back.stc.LoggerMethodeUtils.logStartMethod;

@Component
public class MoneticoClient implements PaiementClient {

    private static Logger LOGGER = LoggerFactory.getLogger(ReferenceFactoryService.class);

    private final WebTarget target;
    private final String tpe;
    private final String companyCode;

    private final MoneticoPropertiesResolver moneticoPropertiesResolver;

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");

    public MoneticoClient(Proxy proxy, MoneticoPropertiesResolver moneticoPropertiesResolver) {

        ClientConfig config = new ClientConfig();

        HttpUrlConnectorProvider cp = new HttpUrlConnectorProvider();
        config.connectorProvider(cp);
        cp.connectionFactory(url -> (HttpURLConnection) url.openConnection(proxy));
        config.register(JacksonJsonProvider.class);
        Client client = ClientBuilder.newClient();

        this.tpe = moneticoPropertiesResolver.getTpe();
        this.companyCode = moneticoPropertiesResolver.getCompanyCode();

        this.target = client.target(moneticoPropertiesResolver.getServiceUrl());
        this.moneticoPropertiesResolver = moneticoPropertiesResolver;
    }

    @Retryable(value = Exception.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    public String capture(MoyenPaiementBO paiement, double montant) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ MoyenPaiementBO {}] ", paiement);
        Date date = new Date(System.currentTimeMillis());
        String dateTimeString = simpleDateFormat.format(date);


        Response response = this.target.queryParam("TPE", this.tpe)
                .queryParam("montant", paiement.getMontantInitial() + moneticoPropertiesResolver.getCurrency())
                .queryParam("montant_a_capturer", montant + moneticoPropertiesResolver.getCurrency())
                .queryParam("montant_deja_capture", paiement.getMontantCapture() + moneticoPropertiesResolver.getCurrency())
                .queryParam("montant_restant", paiement.getMontantRestant() + moneticoPropertiesResolver.getCurrency())
                .queryParam("lgue", "FR")
                .queryParam("reference", paiement.getPkMoyenPaiement()).queryParam("date", dateTimeString)
                .queryParam("date_commande", dateTimeString).queryParam("societe", this.companyCode)
                .queryParam("version", moneticoPropertiesResolver.getVersion()).request(MediaType.APPLICATION_JSON).get();

        String responseString = response.readEntity(String.class);
        LOGGER.info("Return [ responseString {}] ", responseString);
        return responseString;
    }

}

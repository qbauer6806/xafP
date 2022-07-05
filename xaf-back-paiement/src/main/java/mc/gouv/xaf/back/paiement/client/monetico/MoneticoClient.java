package mc.gouv.xaf.back.paiement.client.monetico;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import mc.gouv.xaf.back.paiement.client.PaiementClient;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
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
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.Date;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Component
public class MoneticoClient implements PaiementClient {

    private static Logger LOGGER = LoggerFactory.getLogger(ReferenceFactoryService.class);

    private final WebTarget target;
    private final String tpe;
    private final String companyCode;

    private final PaiementPropertiesResolver paiementPropertiesResolver;

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");

    public MoneticoClient(Proxy proxy, PaiementPropertiesResolver paiementPropertiesResolver) {

        ClientConfig config = new ClientConfig();

        HttpUrlConnectorProvider cp = new HttpUrlConnectorProvider();
        config.connectorProvider(cp);
        //todo fix proxy
        cp.connectionFactory(url -> (HttpURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy-infra", 3129))));
        config.register(JacksonJsonProvider.class);
        Client client = ClientBuilder.newClient();

        this.tpe = paiementPropertiesResolver.getTpe();
        this.companyCode = paiementPropertiesResolver.getCompanyCode();

        this.target = client.target(paiementPropertiesResolver.getPaiementUrl());
        this.paiementPropertiesResolver = paiementPropertiesResolver;
    }

    @Retryable(value = Exception.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    public String capture(MoyenPaiementBO paiement, double montant) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ MoyenPaiementBO {}] ", paiement);
        Date date = new Date(System.currentTimeMillis());
        String dateTimeString = simpleDateFormat.format(date);


        Response response = this.target.queryParam("TPE", this.tpe)
                .queryParam("montant", paiement.getMontantInitial() + paiementPropertiesResolver.getCurrency())
                .queryParam("montant_a_capturer", montant + paiementPropertiesResolver.getCurrency())
                .queryParam("montant_deja_capture", paiement.getMontantCapture() + paiementPropertiesResolver.getCurrency())
                .queryParam("montant_restant", paiement.getMontantRestant() + paiementPropertiesResolver.getCurrency())
                .queryParam("lgue", "FR")
                .queryParam("reference", paiement.getPkMoyenPaiement()).queryParam("date", dateTimeString)
                .queryParam("date_commande", dateTimeString)
                .queryParam("societe", this.companyCode)
                .queryParam("version", paiementPropertiesResolver.getVersion()).request(MediaType.APPLICATION_JSON).get();

        String responseString = response.readEntity(String.class);
        LOGGER.info("Return [ responseString {}] ", responseString);
        return responseString;
    }

}

package mc.gouv.xaf.back.paiement.client.cir;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import mc.gouv.xaf.back.paiement.client.FactureClient;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Component
public class CirClient implements FactureClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(CirClient.class);
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CHECK_ROUTE = "v1/ts/ecritures/paiement/check";
    public static final String PAIEMENT_ROUTE = "v1/ts/ecritures/paiement/";
    public static final String FACTURE_ROUTE = "v1/ts/ecritures/getfacture";

    private final WebTarget targetCheck;
    private final WebTarget targetCreate;
    private final WebTarget targetGet;

    private final UsagersCache usagersCache;

    private final PaiementPropertiesResolver paiementPropertiesResolver;

    public CirClient(UsagersCache usagersCache, Proxy proxy, PaiementPropertiesResolver paiementPropertiesResolver) {
        String serviceUrl = paiementPropertiesResolver.getFactureUrl();
        ClientConfig config = new ClientConfig();

        HttpUrlConnectorProvider cp = new HttpUrlConnectorProvider();
        config.connectorProvider(cp);
        cp.connectionFactory(url -> (HttpURLConnection) url.openConnection(proxy));

        config.register(JacksonJsonProvider.class);
        Client client = ClientBuilder.newClient();
        this.targetCheck = client.target(serviceUrl + CHECK_ROUTE);
        this.targetCreate = client.target(serviceUrl + PAIEMENT_ROUTE);
        this.targetGet = client.target(serviceUrl + FACTURE_ROUTE);
        this.usagersCache = usagersCache;
        this.paiementPropertiesResolver = paiementPropertiesResolver;
    }

    @Override
    public String check(String numFacture) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ numFacture {}] ", numFacture);
        Response response = this.targetCheck.queryParam("numFacture", numFacture)
                .queryParam("registre", paiementPropertiesResolver.getRegistre())
                .request()
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + paiementPropertiesResolver.getFactureToken())
                .get();

        String responseString = response.readEntity(String.class);
        LOGGER.info("return :" + responseString);
        return responseString;
    }



    @Override
    public String createFacture(String numPermis, String numImmat, Double montant, String codeTransaction, Integer usagerId, HashMap<String, Double> objetMontants) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ numPermis {}, numImmat {},  codeTransaction {}] ", numPermis, numImmat, codeTransaction);

        List<CirRequest> cirRequests = new ArrayList<>();

        for (Map.Entry<String, Double> entry : objetMontants.entrySet()) {
            Double montantObjet = entry.getValue();

            CirRequest request = new CirRequest();
            request.setNumTpe(paiementPropertiesResolver.getTpe());

            request.setNumPermis(numPermis);
            request.setNumImmat(numImmat);


            request.setRegistre(paiementPropertiesResolver.getRegistre());
            request.setDateOperation(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            request.setMontant(montant);
            request.setMontantOperation("" + montantObjet);
            GichuniUsagerDTO usager = usagersCache.get(usagerId);
            request.setNomPropr(usager.getNom());
            request.setPrenomPropr(usager.getPrenom());
            request.setEmail(usager.getEmail());
            request.setCodeOperation(montantObjet == 80 ? "P1" : "P5");
            request.setCodeTransaction(codeTransaction);
            request.setCodeReglement("X");
            request.setAutorisation("6"); // num aut paiement
            request.setTransactionId("6"); //  ref pk operation

            cirRequests.add(request);
        }

        Response response = this.targetCreate
                .request()
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + paiementPropertiesResolver.getFactureToken())
                .post(Entity.entity(cirRequests, MediaType.APPLICATION_JSON));

        String responseString = response.readEntity(String.class);
        LOGGER.info("return :" + responseString);
        return responseString;
    }

    @Override
    public InputStream getFacture(String numFacture) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ numFacture {}] ", numFacture);
        LOGGER.info("Properties [ registre {}] ", paiementPropertiesResolver.getRegistre());
        Response response = this.targetGet.queryParam("numFacture", "" + numFacture)
                .queryParam("registre", "" + paiementPropertiesResolver.getRegistre())
                .request("application/pdf")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + paiementPropertiesResolver.getFactureToken())
                .get();
        LOGGER.info("result [ {}] ", response.getStatus() == Response.Status.OK.getStatusCode());
        InputStream inputStream = response.readEntity(InputStream.class);
        return inputStream;
    }
}

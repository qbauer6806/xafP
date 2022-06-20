package mc.gouv.xaf.back.stc.client.cir;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static mc.gouv.xaf.back.stc.LoggerMethodeUtils.logStartMethod;

public class CirClient {

    private static Logger LOGGER = LoggerFactory.getLogger(CirClient.class);
    public static final String BEARER_PREFIX = "Bearer ";
    private String serviceUrl;
    private WebTarget target;
    private Client client;

    public CirClient(String route) {
        this.serviceUrl = CirConfiguration.URL + route;
        ClientConfig config = new ClientConfig();

        HttpUrlConnectorProvider cp = new HttpUrlConnectorProvider();
        config.connectorProvider(cp);
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy-infra", 3129));
        cp.connectionFactory(url -> (HttpURLConnection) url.openConnection(proxy));

        config.register(JacksonJsonProvider.class);
        this.client = ClientBuilder.newClient();
        this.target = this.client.target(serviceUrl);
    }

    public static String check(String numFacture) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ numFacture {}] ", numFacture);
        CirClient cirClient = new CirClient(CirConfiguration.CHECK_ROUTE);
        Response response = cirClient.target.queryParam("numFacture", numFacture)
                .queryParam("registre", CirConfiguration.REGISTRE)
                .queryParam("codeTarif", CirConfiguration.CODE_TARIF)
                .request()
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + CirConfiguration.TOKEN)
                .get();

        String responseString = response.readEntity(String.class);
        LOGGER.info("return :" + responseString);
        return responseString;
    }

    public static String postPaiement(String numPermis, String numImmat, double montant, String codeTransaction) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ numPermis {}, numImmat {}, montant {}, codeTransaction {}] ", numPermis, numImmat, montant, codeTransaction);
        CirClient cirClient = new CirClient(CirConfiguration.PAIEMENT_ROUTE);

        List<CirRequest> cirRequests = new ArrayList<>();
        CirRequest request = new CirRequest();
        request.setNumTpe(CirConfiguration.TPE_CONFIGURATION);

        if (numPermis != null) {
            request.setNumPermis(numPermis);
        } else {
            request.setNumImmat(numImmat);
        }
        request.setRegistre(CirConfiguration.REGISTRE);
        request.setDateOperation(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        request.setMontant(montant);
        request.setMontantOperation("" + montant);
        request.setNomPropr("GERMAIN");
        request.setPrenomPropr("Edouard");
        request.setEmail("egermain.ext@gouv.mc");
        request.setCodeOperation(CirConfiguration.CODE_TARIF);
        request.setCodeTransaction(codeTransaction);
        request.setCodeReglement("X");
        request.setAutorisation("6");
        request.setTransactionId("6");

        cirRequests.add(request);
        Response response = cirClient.target
                .request()
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + CirConfiguration.TOKEN)
                .post(Entity.entity(cirRequests, MediaType.APPLICATION_JSON));

        String responseString = response.readEntity(String.class);
        LOGGER.info("return :" + responseString);
        return responseString;
    }

    public static InputStream getFacture(String numFacture) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ numFacture {}] ", numFacture);
        CirClient cirClient1 = new CirClient(CirConfiguration.CHECK_ROUTE);
        Response response1 = cirClient1.target.queryParam("numFacture", ""+1124807)
                .queryParam("registre", ""+CirConfiguration.REGISTRE)
                .request("application/json")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + CirConfiguration.TOKEN)
                .get();

        String result = response1.readEntity(String.class);
        LOGGER.info("result [ {}] ", result);

        CirClient cirClient = new CirClient(CirConfiguration.FACTURE_ROUTE);
        Response response = cirClient.target.queryParam("numFacture", ""+1124807)
                .queryParam("registre", ""+CirConfiguration.REGISTRE)
                .request("application/pdf")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + CirConfiguration.TOKEN)
                .get();
        LOGGER.info("result2 [ {}] ", response.getStatus() == Response.Status.OK.getStatusCode());
        InputStream inputStream = response.readEntity(InputStream.class);
        return inputStream;
    }
}

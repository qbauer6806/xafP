package mc.gouv.xaf.back.paiement.service.impl;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logEndMethod;
import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import mc.gouv.xaf.back.paiement.dto.itg.cir.CirRequestDTO;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.retry.Operation;
import mc.gouv.xaf.back.paiement.retry.OperationHelper;
import mc.gouv.xaf.back.paiement.service.itg.FactureApiClient;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.enums.MailSupportEnum;
import org.apache.http.client.HttpResponseException;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FactureApiClientImpl implements FactureApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactureApiClientImpl.class);
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CHECK_ROUTE = "v1/ts/ecritures/paiement/check";
    public static final String PAIEMENT_ROUTE = "v1/ts/ecritures/paiement";
    public static final String FACTURE_ROUTE = "v1/ts/ecritures/getfacture";
    public static final String PERMIS_ROUTE = "v1/permis/{numPermis}";
    private final WebTarget targetCheck;
    private final WebTarget targetCreate;
    private final WebTarget targetGet;
    private final OperationHelper operationHelper;
    private final PaiementPropertiesResolver paiementPropertiesResolver;
    private final MailService mailService;

    public FactureApiClientImpl(PaiementPropertiesResolver paiementPropertiesResolver, OperationHelper operationHelper,
            MailService mailService) {
        String serviceUrl = paiementPropertiesResolver.getFactureUrl();
        ClientConfig config = new ClientConfig();

        HttpUrlConnectorProvider cp = new HttpUrlConnectorProvider();
        config.connectorProvider(cp);
        cp.connectionFactory(url -> (HttpURLConnection) url.openConnection());

        config.register(JacksonJsonProvider.class);
        try (Client client = ClientBuilder.newClient()) {
            this.targetCheck = client.target(serviceUrl + CHECK_ROUTE);
            this.targetCreate = client.target(serviceUrl + PAIEMENT_ROUTE);
            this.targetGet = client.target(serviceUrl + FACTURE_ROUTE);
        }
        this.paiementPropertiesResolver = paiementPropertiesResolver;
        this.operationHelper = operationHelper;
        this.mailService = mailService;
    }

    @Override
    public String check(String numFacture) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ numFacture {}] ", numFacture);
        Response response = this.targetCheck.queryParam("numFacture", numFacture)
                .queryParam("registre", paiementPropertiesResolver.getRegistre()).request()
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + paiementPropertiesResolver.getFactureToken()).get();

        String responseString = response.readEntity(String.class);
        LOGGER.info("return : {}", responseString);
        return responseString;
    }

    @Override
    public Optional<String> createFacture(List<CirRequestDTO> lignes, DemandeDTO demandeDTO) {
        logStartMethod(LOGGER);
        Operation<String> operation = new Operation<>() {

            @Override
            public void execute() throws HttpResponseException {
                Response response = targetCreate.request()
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + paiementPropertiesResolver.getFactureToken())
                        .post(Entity.entity(lignes, MediaType.APPLICATION_JSON));

                if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                    throw new HttpResponseException(response.getStatus(), "CIR createFacture() failed");
                }
                setResult(response.readEntity(String.class));
            }

            @Override
            public Logger getLogger() {
                return LOGGER;
            }
        };

        try {
            operationHelper.executeWithRetry(operation);
            LOGGER.info("return : {}", operation.getResult());
            return operation.getResult();
        } catch (Exception e) {
            sendMail(demandeDTO, operation, 6);
        }
        return Optional.empty();
    }

    @Override
    public Optional<InputStream> getFacture(String numFacture, DemandeDTO demandeDTO) throws HttpResponseException {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ numFacture {}] ", numFacture);

        Operation<InputStream> operation = new Operation<>() {

            @Override
            public void execute() throws HttpResponseException {
                Response response = targetGet.queryParam("numFacture", numFacture).request("application/pdf")
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + paiementPropertiesResolver.getFactureToken())
                        .get();

                if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                    throw new HttpResponseException(response.getStatus(), "CIR getFacture() failed");
                }

                InputStream inputStream = response.readEntity(InputStream.class);
                setResult(inputStream);
                logEndMethod(getLogger());
            }

            @Override
            public Logger getLogger() {
                return LOGGER;
            }
        };

        try {
            operationHelper.executeWithRetry(operation);
            logEndMethod(LOGGER);
            return operation.getResult();
        } catch (Exception e) {
            sendMail(demandeDTO, operation, 7);
            throw e;
        }
    }

    private void sendMail(DemandeDTO demandeDTO, Operation<?> operation, int incident) {
        String bodyTemplateCode = "MAIL_CIR_ECHEC_CORPS";
        String subjectTemplateCode = "MAIL_CIR_ECHEC_OBJET";
        Set<String> mailingLists = mailService.getMailingLists(
                MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE.name(),
                MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE_CIR.name(),
                MailSupportEnum.XAF_ADRESSES_MAIL_ADMIN_METIER.name());
        Map<String, Object> model = new HashMap<>();
        model.put("resultat", operation.getResult());
        mailService.sendMailSupport(subjectTemplateCode, bodyTemplateCode, mailingLists, demandeDTO.getPkDemandes(),
                demandeDTO.getIdentifiant(), incident, model, null);
    }
}

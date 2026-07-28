package mc.gouv.xaf.back.paiement.service.impl;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logEndMethod;
import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

import java.io.InputStream;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FactureApiClientImpl implements FactureApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactureApiClientImpl.class);

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CHECK_ROUTE = "v1/ts/ecritures/paiement/check";
    public static final String PAIEMENT_ROUTE = "v1/ts/ecritures/paiement";
    public static final String FACTURE_ROUTE = "v1/ts/ecritures/getfacture";
    public static final String PERMIS_ROUTE = "v1/permis/{numPermis}";

    private final RestClient restClient;
    private final OperationHelper operationHelper;
    private final PaiementPropertiesResolver paiementPropertiesResolver;
    private final MailService mailService;

    public FactureApiClientImpl(PaiementPropertiesResolver paiementPropertiesResolver, OperationHelper operationHelper,
            MailService mailService) {
        this.paiementPropertiesResolver = paiementPropertiesResolver;
        this.operationHelper = operationHelper;
        this.mailService = mailService;

        this.restClient = RestClient.builder().baseUrl(paiementPropertiesResolver.getFactureUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + paiementPropertiesResolver.getFactureToken())
                .build();
    }

    @Override
    public String check(String numFacture) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ numFacture {}] ", numFacture);

        String responseString = restClient.get()
                .uri(uriBuilder -> uriBuilder.path(CHECK_ROUTE).queryParam("numFacture", numFacture)
                        .queryParam("registre", paiementPropertiesResolver.getRegistre()).build()).retrieve()
                .body(String.class);

        LOGGER.info("return : {}", responseString);
        return responseString;
    }

    @Override
    public Optional<String> createFacture(List<CirRequestDTO> lignes, DemandeDTO demandeDTO) {
        logStartMethod(LOGGER);

        Operation<String> operation = new Operation<>() {

            @Override
            public void execute() throws HttpResponseException {
                String responseBody = restClient.post().uri(PAIEMENT_ROUTE).contentType(MediaType.APPLICATION_JSON)
                        .body(lignes).exchange((request, response) -> {
                            if (!response.getStatusCode().isSameCodeAs(HttpStatus.CREATED)) {
                                throw new HttpResponseException(response.getStatusCode().value(),
                                        "CIR createFacture() failed");
                            }
                            return new String(response.getBody().readAllBytes());
                        });

                setResult(responseBody);
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
                InputStream inputStream = restClient.get()
                        .uri(uriBuilder -> uriBuilder.path(FACTURE_ROUTE).queryParam("numFacture", numFacture).build())
                        .accept(MediaType.APPLICATION_PDF).exchange((request, response) -> {
                            if (!response.getStatusCode().isSameCodeAs(HttpStatus.OK)) {
                                throw new HttpResponseException(response.getStatusCode().value(),
                                        "CIR getFacture() failed");
                            }

                            return response.getBody();
                        });

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

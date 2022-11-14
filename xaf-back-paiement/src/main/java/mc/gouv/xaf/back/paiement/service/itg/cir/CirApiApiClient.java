package mc.gouv.xaf.back.paiement.service.itg.cir;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;
import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logEndMethod;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import mc.gouv.xaf.back.paiement.dto.CommandeDemandeArticleDTO;
import mc.gouv.xaf.shared.enums.MailSupportEnum;
import org.apache.http.client.HttpResponseException;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.dto.InformationFacturationDTO;
import mc.gouv.xaf.back.paiement.dto.itg.cir.CirRequestDTO;
import mc.gouv.xaf.back.paiement.dto.itg.cir.PermisDTO;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.retry.Operation;
import mc.gouv.xaf.back.paiement.retry.OperationHelper;
import mc.gouv.xaf.back.paiement.service.itg.FactureApiClient;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.shared.dto.DemandeDTO;

@Component
public class CirApiApiClient implements FactureApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CirApiApiClient.class);
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CHECK_ROUTE = "v1/ts/ecritures/paiement/check";
    public static final String PAIEMENT_ROUTE = "v1/ts/ecritures/paiement/";
    public static final String FACTURE_ROUTE = "v1/ts/ecritures/getfacture";
    public static final String PERMIS_ROUTE = "v1/permis/{numPermis}";
    private final WebTarget targetCheck;
    private final WebTarget targetCreate;
    private final WebTarget targetGet;
    private final WebTarget targetGetPermis;
    private final OperationHelper operationHelper;
    private final PaiementPropertiesResolver paiementPropertiesResolver;
    private final MailService mailService;

    public CirApiApiClient(Proxy proxy,
                           PaiementPropertiesResolver paiementPropertiesResolver,
                           OperationHelper operationHelper,
                           MailService mailService) {
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
        this.targetGetPermis = client.target(serviceUrl + PERMIS_ROUTE);
        this.paiementPropertiesResolver = paiementPropertiesResolver;
        this.operationHelper = operationHelper;
        this.mailService = mailService;
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
        LOGGER.info("return : {}", responseString);
        return responseString;
    }

    @Override
    public Optional<String> createFacture(String numPermis, String numImmat, double montant, String codeTransaction, InformationFacturationDTO infoFacturation, List<CommandeDemandeArticleDTO> articles, DemandeDTO demandeDTO, CommandeOperationDTO commandeOperationDto) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ numPermis {}, numImmat {},  codeTransaction {}] ", numPermis, numImmat, codeTransaction);

        List<CirRequestDTO> cirRequestDTOS = new ArrayList<>();

        // Récupération des propriétés
        String tpe = paiementPropertiesResolver.getTpe();
        int registre = paiementPropertiesResolver.getRegistre();
        String codePaiement = paiementPropertiesResolver.getCodePaiement();

        for (CommandeDemandeArticleDTO article : articles) {
            CirRequestDTO request = new CirRequestDTO();
            request.setNumTpe(tpe);
            request.setNumPermis(numPermis);
            request.setNumImmat(numImmat);
            request.setRegistre(registre);
			try {
				Date currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(commandeOperationDto.getDateCreation().toString());
				request.setDateOperation(new SimpleDateFormat("yyyy-MM-dd").format(currentDate));
			} catch (ParseException e) {
				LOGGER.info("Erreur lors du parsing de la date de creation de l'opération : {}", commandeOperationDto.getPkOperations());
			}
            request.setMontant(montant);
            request.setMontantOperation("" + article.getMontant());
            request.setNomPropr(infoFacturation.getNomTitulaire());
            request.setPrenomPropr(infoFacturation.getPrenomTitulaire());
            request.setEmail(infoFacturation.getEmailUsager());
            request.setCodeOperation(article.getCodeTarif());
            request.setCodeTransaction(codeTransaction);
            request.setCodeReglement(codePaiement);
            request.setAutorisation("" + commandeOperationDto.getNumeroAutorisation());
            request.setTransactionId(commandeOperationDto.getPkOperations());
            cirRequestDTOS.add(request);
        }

        Operation<String> operation = new Operation<String>() {
            @Override
            public void execute() throws Exception {
                Response response = targetCreate
                        .request()
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + paiementPropertiesResolver.getFactureToken())
                        .post(Entity.entity(cirRequestDTOS, MediaType.APPLICATION_JSON));

                if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                    throw new HttpResponseException(response.getStatus(), "CIR createFacture() failed");
                }

                // Propriétés de tests pour bloquer les appels d'API
//                PropertiesDTO errorProp = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), "TEMP_FAIL_CIR_ECRITURE_COMPTABLE");
//                if (response.getStatus() != Response.Status.CREATED.getStatusCode() || (errorProp != null && "true".equals(errorProp.getValue()))) {
//                    throw new HttpResponseException(response.getStatus(), "CIR createFacture() failed");
//                }
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
    public Optional<InputStream> getFacture(String numFacture, DemandeDTO demandeDTO) throws Exception {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ numFacture {}] ", numFacture);
        LOGGER.info("Properties [ registre {}] ", paiementPropertiesResolver.getRegistre());


        Operation<InputStream> operation = new Operation<InputStream>() {
            @Override
            public void execute() throws Exception {
                Response response = targetGet.queryParam("numFacture", "" + numFacture)
                        .queryParam("registre", "" + paiementPropertiesResolver.getRegistre())
                        .request("application/pdf")
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + paiementPropertiesResolver.getFactureToken())
                        .get();

                if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                    throw new HttpResponseException(response.getStatus(), "CIR getFacture() failed");
                }

                // Propriétés de tests pour bloquer les appels d'API
//                PropertiesDTO errorProp = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), "TEMP_FAIL_CIR_RECUP_FACTURE");
//                if (response.getStatus() != Response.Status.OK.getStatusCode() || (errorProp != null && "true".equals(errorProp.getValue()))) {
//                    throw new HttpResponseException(response.getStatus(), "CIR getFacture() failed");
//                }
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

    @Override
    public PermisDTO getPermis(String numPermis) throws Exception {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ getPermis {}] ", numPermis);

        Response response = targetGetPermis.resolveTemplate("numPermis", numPermis)
                .request()
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + paiementPropertiesResolver.getFactureToken())
                .get();

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new HttpResponseException(response.getStatus(), response.toString());
        }

        // Propriétés de tests pour bloquer les appels d'API
//        PropertiesDTO errorProp = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), "TEMP_FAIL_CIR_TABLE_PERMIS");
//        if (response.getStatus() != Response.Status.OK.getStatusCode() || (errorProp != null && "true".equals(errorProp.getValue()))) {
//            throw new HttpResponseException(500, response.toString());
//        }
        PermisDTO permisDTO = response.readEntity(PermisDTO.class);
        logEndMethod(LOGGER);
        return permisDTO;
    }

    private void sendMail(DemandeDTO demandeDTO, Operation<?> operation, int incident) {
        String bodyTemplateCode = "MAIL_CIR_ECHEC_CORPS";
        String subjectTemplateCode = "MAIL_CIR_ECHEC_OBJET";
        Set<String> mailingLists = mailService.getMailingLists(MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE.name(), MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE_CIR.name(), MailSupportEnum.XAF_ADRESSES_MAIL_ADMIN_METIER.name());
        Map<String, Object> model = new HashMap<>();
        model.put("resultat", operation.getResult());
        mailService.sendMailSupport(subjectTemplateCode, bodyTemplateCode, mailingLists, demandeDTO.getPkDemandes(), demandeDTO.getIdentifiant(), incident, model, null);
    }
}

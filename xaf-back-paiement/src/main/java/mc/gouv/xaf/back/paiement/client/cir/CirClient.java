package mc.gouv.xaf.back.paiement.client.cir;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import mc.gouv.xaf.back.paiement.client.FactureClient;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.retry.Operation;
import mc.gouv.xaf.back.paiement.retry.OperationHelper;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
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
import java.util.Optional;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Component
public class CirClient implements FactureClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(CirClient.class);
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CHECK_ROUTE = "v1/ts/ecritures/paiement/check";
    public static final String PAIEMENT_ROUTE = "v1/ts/ecritures/paiement/";
    public static final String FACTURE_ROUTE = "v1/ts/ecritures/getfacture";
    public static final String XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE = "XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE";


    private static SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");

    private final WebTarget targetCheck;
    private final WebTarget targetCreate;
    private final WebTarget targetGet;

    private final UsagersCache usagersCache;

    private final OperationHelper operationHelper;

    private final PaiementPropertiesResolver paiementPropertiesResolver;

    private MailService mailService;
    private AfBackUtils afBackUtils;
    private PropertiesService propertiesService;
    private GouvPropertiesResolver gouvPropertiesResolver;

    public CirClient(UsagersCache usagersCache, Proxy proxy,
                     PaiementPropertiesResolver paiementPropertiesResolver,
                     OperationHelper operationHelper,
                     MailService mailService,
                     AfBackUtils afBackUtils,
                     PropertiesService propertiesService,
                     GouvPropertiesResolver gouvPropertiesResolver) {
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
        this.operationHelper = operationHelper;
        this.mailService = mailService;
        this.afBackUtils = afBackUtils;
        this.propertiesService = propertiesService;
        this.gouvPropertiesResolver = gouvPropertiesResolver;
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
    public Optional<String> createFacture(String numPermis, String numImmat, Double montant, String codeTransaction, Integer usagerId, HashMap<String, Double> objetMontants, DemandeDTO demandeDTO, OperationBO operationBO) {
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

            PropertiesDTO montantProperty = propertiesService.getProperty("PERMC", "XAF_PAIEMENT_AMOUNT");
            double prix = Double.parseDouble(montantProperty.getValue());

            request.setCodeOperation(montantObjet == prix ? "P1" : "P5"); //voir avec alexis devrait être dans les properties
            request.setCodeTransaction(codeTransaction);
            request.setCodeReglement("X"); // idem devrait être en properties meme si c'est fixe
            request.setAutorisation("" + operationBO.getNumeroAuthorisation());
            request.setTransactionId(operationBO.getPkOperation());

            cirRequests.add(request);
        }
        Operation<String> operation = new Operation<String>() {
            @Override
            public void execute() throws Exception {
                Response response = targetCreate
                        .request()
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + paiementPropertiesResolver.getFactureToken())
                        .post(Entity.entity(cirRequests, MediaType.APPLICATION_JSON));

                if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                    throw new RuntimeException("CIR createFacture() failed");
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

            LOGGER.info("return :" + operation.getResult());
            return operation.getResult();
        } catch (Exception e) {
            sendMailTechnique(demandeDTO, operation, 6);
            sendMailFonctionnel(demandeDTO, operation, 6);
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
                    throw new RuntimeException("CIR getFacture() failed");
                }
                InputStream inputStream = response.readEntity(InputStream.class);
                setResult(inputStream);

            }

            @Override
            public Logger getLogger() {
                return LOGGER;
            }
        };

        try {
            operationHelper.executeWithRetry(operation);
            return operation.getResult();
        } catch (Exception e) {
            sendMailTechnique(demandeDTO, operation, 7);
            sendMailFonctionnel(demandeDTO, operation, 7);
            throw e;
        }
    }

    private void sendMailTechnique(DemandeDTO demandeDTO, Operation<?> operation, int incident) {
        Date date = new Date(System.currentTimeMillis());
        String dateTimeString = simpleDateTimeFormat.format(date);
        String bodyTemplateCode = "MAIL_CIR_ECHEC_TECH_CORPS";
        String subjectTemplateCode = "MAIL_CIR_ECHEC_TECH_OBJET";

        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(), afBackUtils.getDemarcheInfos()
                .getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(), afBackUtils.getDemarcheInfos()
                .getEmailReplytoNom());

        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE);

        if (propertiesDTO.getValue() != null) {
            String[] adresses = propertiesDTO.getValue().trim().split(",");

            for (String adresseMail : adresses) {
                emailInfo.addTo(adresseMail, "Support Technique");
            }
        }

        emailInfo.setLangue("fr");


        Map<String, Object> model = new HashMap<>();
        model.put("incident", incident);
        model.put("dateTimeString", dateTimeString);
        model.put("PkDemandes", demandeDTO.getPkDemandes());
        model.put("reponse", operation.getResult());
        try {
            mailService.sendMail(emailInfo, model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }
    }

    private void sendMailFonctionnel(DemandeDTO demandeDTO, Operation<?> operation, int incident) {
        Date date = new Date(System.currentTimeMillis());
        String dateTimeString = simpleDateTimeFormat.format(date);
        String bodyTemplateCode = "MAIL_CIR_ECHEC_FONC_CORPS";
        String subjectTemplateCode = "MAIL_CIR_ECHEC_FONC_OBJET";

        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(), afBackUtils.getDemarcheInfos()
                .getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(), afBackUtils.getDemarcheInfos()
                .getEmailReplytoNom());

        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE);

        if (propertiesDTO.getValue() != null) {
            String[] adresses = propertiesDTO.getValue().trim().split(",");

            for (String adresseMail : adresses) {
                emailInfo.addTo(adresseMail, "Support Technique");
            }
        }

        emailInfo.setLangue("fr");


        Map<String, Object> model = new HashMap<>();
        model.put("incident", incident);
        model.put("dateTimeString", dateTimeString);
        model.put("PkDemandes", demandeDTO.getPkDemandes());
        model.put("reponse", operation.getResult());
        try {
            mailService.sendMail(emailInfo, model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }
    }
}

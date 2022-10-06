package mc.gouv.xaf.back.paiement.service.itg.cir;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import mc.gouv.xaf.back.paiement.service.MontantService;
import org.apache.commons.lang3.StringUtils;
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
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;

@Component
public class CirApiApiClient implements FactureApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CirApiApiClient.class);
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CHECK_ROUTE = "v1/ts/ecritures/paiement/check";
    public static final String PAIEMENT_ROUTE = "v1/ts/ecritures/paiement/";
    public static final String FACTURE_ROUTE = "v1/ts/ecritures/getfacture";
    public static final String PERMIS_ROUTE = "v1/permis/{numPermis}";
    public static final String XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE = "XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE";


    private static SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");

    private final WebTarget targetCheck;
    private final WebTarget targetCreate;
    private final WebTarget targetGet;
    private final WebTarget targetGetPermis;

    private final OperationHelper operationHelper;

    private final PaiementPropertiesResolver paiementPropertiesResolver;

    private MailService mailService;
    private AfBackUtils afBackUtils;
    private PropertiesService propertiesService;
    private GouvPropertiesResolver gouvPropertiesResolver;
    private MontantService montantService;

    public CirApiApiClient(Proxy proxy,
                           PaiementPropertiesResolver paiementPropertiesResolver,
                           OperationHelper operationHelper,
                           MailService mailService,
                           AfBackUtils afBackUtils,
                           PropertiesService propertiesService,
                           GouvPropertiesResolver gouvPropertiesResolver,
                           MontantService montantService) {
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
        this.afBackUtils = afBackUtils;
        this.propertiesService = propertiesService;
        this.gouvPropertiesResolver = gouvPropertiesResolver;
        this.montantService = montantService;
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
    public Optional<String> createFacture(String numPermis, String numImmat, Double montant, String codeTransaction, InformationFacturationDTO infoFacturation, HashMap<String, BigDecimal> objetMontants, DemandeDTO demandeDTO, CommandeOperationDTO commandeOperationDto) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ numPermis {}, numImmat {},  codeTransaction {}] ", numPermis, numImmat, codeTransaction);

        List<CirRequestDTO> cirRequestDTOS = new ArrayList<>();

        // Récupération des propriétés
        String tpe = paiementPropertiesResolver.getTpe();
        int registre = paiementPropertiesResolver.getRegistre();
        String codePaiement = paiementPropertiesResolver.getCodePaiement();

        for (Map.Entry<String, BigDecimal> entry : objetMontants.entrySet()) {
            double montantObjet = entry.getValue().doubleValue();
            String montantKey = entry.getKey();

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
            request.setMontantOperation("" + montantObjet);
            request.setNomPropr(StringUtils.stripAccents(infoFacturation.getNomTitulaire().toUpperCase()));
            // si plusieurs prenom sur mconnect, prendre le 1er (xavier,guillaume prendre xavier) + enlever les accents
            request.setPrenomPropr(StringUtils.stripAccents(infoFacturation.getPrenomTitulaire().toUpperCase().split(",")[0]));
            request.setEmail(infoFacturation.getEmailUsager());
            request.setCodeOperation(montantService.getCodeFacturation(montantKey));
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
                    throw new HttpResponseException(response.getStatus(), "CIR getFacture() failed");
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

    @Override
    public Optional<PermisDTO> getPermis(String numPermis) throws Exception {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ getPermis {}] ", numPermis);

        Operation<PermisDTO> operation = new Operation<PermisDTO>() {
            @Override
            public void execute() throws Exception {
                Response response = targetGetPermis.resolveTemplate("numPermis", numPermis)
                        .request()
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + paiementPropertiesResolver.getFactureToken())
                        .get();


                if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                    throw new HttpResponseException(response.getStatus(), "CIR getPermis() failed");
                }
                PermisDTO permisDTO = response.readEntity(PermisDTO.class);
                setResult(permisDTO);

            }

            @Override
            public Logger getLogger() {
                return LOGGER;
            }
        };

        operationHelper.executeWithRetry(operation);
        return operation.getResult();
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

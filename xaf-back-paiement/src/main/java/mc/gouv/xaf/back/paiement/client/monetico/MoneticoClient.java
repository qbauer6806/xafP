package mc.gouv.xaf.back.paiement.client.monetico;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import mc.gouv.xaf.back.paiement.client.PaiementClient;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementStatutBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationStatutBO;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.retry.Operation;
import mc.gouv.xaf.back.paiement.retry.OperationHelper;
import mc.gouv.xaf.back.paiement.service.ReferenceFactoryService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.http.client.HttpResponseException;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Component
public class MoneticoClient implements PaiementClient {

    private static Logger LOGGER = LoggerFactory.getLogger(ReferenceFactoryService.class);

    private final WebTarget target;
    private final String tpe;

    private final PaiementPropertiesResolver paiementPropertiesResolver;

    private final OperationHelper operationHelper;

    private MailService mailService;
    private AfBackUtils afBackUtils;
    private PropertiesService propertiesService;
    private GouvPropertiesResolver gouvPropertiesResolver;

    private static String XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE = "XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE";
    private static SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy:HH:mm:ss");
    private static String XAF_ACTIVATION_CAPTURE_PAIEMENT = "XAF_ACTIVATION_CAPTURE_PAIEMENT";

    public MoneticoClient(Proxy proxy,
                          PaiementPropertiesResolver paiementPropertiesResolver,
                          OperationHelper operationHelper,
                          MailService mailService,
                          AfBackUtils afBackUtils,
                          PropertiesService propertiesService,
                          GouvPropertiesResolver gouvPropertiesResolver) {

        ClientConfig config = new ClientConfig();

        HttpUrlConnectorProvider cp = new HttpUrlConnectorProvider();
        config.connectorProvider(cp);
        cp.connectionFactory(url -> (HttpURLConnection) url.openConnection(proxy));
        config.register(JacksonJsonProvider.class);
        Client client = ClientBuilder.newClient(config);

        this.tpe = paiementPropertiesResolver.getTpe();

        this.target = client.target(paiementPropertiesResolver.getCaptureUrl());
        this.paiementPropertiesResolver = paiementPropertiesResolver;
        this.operationHelper = operationHelper;
        this.mailService = mailService;
        this.afBackUtils = afBackUtils;
        this.propertiesService = propertiesService;
        this.gouvPropertiesResolver = gouvPropertiesResolver;
    }

    public boolean capture(MoyenPaiementBO paiement, OperationBO operationBO, DemandeDTO demandeDTO) {
        logStartMethod(LOGGER);

        LOGGER.info("Parameters [ MoyenPaiementBO {}] ", paiement);
        LOGGER.info("Parameters [ OperationBO {}] ", operationBO);
        LOGGER.info("Parameters [ DemandeDTO {}] ", demandeDTO);

        String dateString = paiement.getCommande().getDateCreation().format(dateFormatter);
        String dateTimeString = paiement.getCommande().getDateCreation().format(dateTimeFormatter);
        Operation<String> operation = new Operation<String>() {
            @Override
            public void execute() throws Exception {

                // Permet de désactiver la capture en simulant une erreur d'opération.
                PropertiesDTO captureActive = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_ACTIVATION_CAPTURE_PAIEMENT);
                if (captureActive != null && !Boolean.parseBoolean(captureActive.getValue())) {
                    // On met le statut 400 pour éviter de faire plusieurs tentatives
                    throw new HttpResponseException(Response.Status.BAD_REQUEST.getStatusCode(), "Capture du paiement désactivé");
                }

                LOGGER.info("URL: {}", paiementPropertiesResolver.getCaptureUrl());
                String tpe = getTpe();
                LOGGER.info("TPE: {}", tpe);
                String montant = paiement.getMontantInitial() + paiementPropertiesResolver.getCurrency();
                LOGGER.info("montant: {}", montant);
                String montantACapturer = operationBO.getMontant() + paiementPropertiesResolver.getCurrency();
                LOGGER.info("montant_a_capturer: {}", montantACapturer);
                String montantDejaCapture = paiement.getMontantCapture() + paiementPropertiesResolver.getCurrency();
                LOGGER.info("montant_deja_capture: {}", montantDejaCapture);
                String montantRestant = (paiement.getMontantRestant() - operationBO.getMontant()) + paiementPropertiesResolver.getCurrency();
                LOGGER.info("montant_restant: {}", montantRestant);
                LOGGER.info("reference: {}", paiement.getPkMoyenPaiement());
                LOGGER.info("date: {}", dateTimeString);
                LOGGER.info("date_commande: {}", dateString);
                LOGGER.info("societe: {}", paiement.getCodeSociete());
                String version = paiementPropertiesResolver.getVersionCapture();
                LOGGER.info("version {}", version)

                Response response = getTarget().queryParam("TPE", tpe)
                        .queryParam("montant", montant)
                        .queryParam("montant_a_capturer", montantACapturer)
                        .queryParam("montant_deja_capture", montantDejaCapture)
                        .queryParam("montant_restant", montantRestant)
                        .queryParam("lgue", "FR")
                        .queryParam("reference", paiement.getPkMoyenPaiement())
                        .queryParam("date", dateTimeString)
                        .queryParam("date_commande", dateString)
                        .queryParam("societe", paiement.getCodeSociete())
                        .queryParam("version", version)
                        .request(MediaType.APPLICATION_JSON).get();

                String responseString = response.readEntity(String.class);
                LOGGER.info("Capture [ responseString {}] ", responseString);
                setResult(responseString);
                extractResult(responseString, operationBO, paiement);

                if (!OperationStatutBO.ACCEPTEE.equals(operationBO.getOperationStatut())) {
                    throw new HttpResponseException(response.getStatus(), "Operation non acceptee");
                }
            }

            @Override
            public Logger getLogger() {
                return LOGGER;
            }
        };

        try {
            operationHelper.executeWithRetry(operation);
        } catch (Exception exception) {
            LOGGER.error("Impossible de faire la capture");
            LOGGER.error(exception.getMessage(), exception);
            //send mail + delete command_demande

            if (mailService != null) {
                if (OperationStatutBO.ERREUR.equals(operationBO.getOperationStatut())) {
                    sendMail(demandeDTO, operation, 4);
                } else {
                    operationBO.setOperationStatut(OperationStatutBO.ERREUR);
                    sendMail(demandeDTO, operation, 3);
                }

            }

            return false;
        }

        return true;
    }

    private void sendMail(DemandeDTO demandeDTO, Operation<String> operation, int incident) {
        Date date = new Date(System.currentTimeMillis());
        String dateTimeString = simpleDateTimeFormat.format(date);
        String bodyTemplateCode = "MAIL_CAPTURE_ECHEC_CORPS";
        String subjectTemplateCode = "MAIL_CAPTURE_ECHEC_OBJET";

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
        model.put("reponse", operation == null ? null : operation.getResult());
        try {
            mailService.sendMail(emailInfo, model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }
    }

    private void extractResult(String responseString, OperationBO operation, MoyenPaiementBO paiement) {
        for (String s : responseString.split("\n")) {
            String[] keyValue = s.split("=");

            switch (keyValue[0]) {
                case "cdr": // cdr = Code retour indiquant le résultat de la capture
                    operation.setCodeRetour(keyValue[1]);
                    if ("1".equals(keyValue[1])) {
                        operation.setOperationStatut(OperationStatutBO.ACCEPTEE);
                    } else if ("0".equals(keyValue[1])) {
                        operation.setOperationStatut(OperationStatutBO.REFUSEE);
                    } else {
                        operation.setOperationStatut(OperationStatutBO.ERREUR);
                    }
                    break;
                case "aut": // aut = Numéro d’autorisation du paiement si celui-ci a été accepté
                    operation.setNumeroAuthorisation(Integer.parseInt(keyValue[1]));
                    break;
               case "lib": // lib = Libellé détaillé précisant la nature du code retour
                    operation.setLibelle(keyValue[1]);
                    break;
            }
        }
    }

    public WebTarget getTarget() {
        return target;
    }

    public String getTpe() {
        return tpe;
    }
}

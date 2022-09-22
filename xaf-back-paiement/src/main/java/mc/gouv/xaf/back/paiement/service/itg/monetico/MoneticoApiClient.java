package mc.gouv.xaf.back.paiement.service.itg.monetico;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.retry.Operation;
import mc.gouv.xaf.back.paiement.retry.OperationHelper;
import mc.gouv.xaf.back.paiement.service.itg.PaiementApiClient;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
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
public class MoneticoApiClient implements PaiementApiClient {

    private static Logger LOGGER = LoggerFactory.getLogger(MoneticoApiClient.class);

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
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy:HH:mm:ss");
    private static String XAF_ACTIVATION_CAPTURE_PAIEMENT = "XAF_ACTIVATION_CAPTURE_PAIEMENT";

    public MoneticoApiClient(Proxy proxy,
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

    public boolean capture(CommandeDTO commandeDTO, CommandeOperationDTO commandeOperationDTO, DemandeDTO demandeDTO) {
        logStartMethod(LOGGER);

        LOGGER.info("Parameters [ CommandeDTO {}] ", commandeDTO);
        LOGGER.info("Parameters [ OperationBO {}] ", commandeOperationDTO);
        LOGGER.info("Parameters [ DemandeDTO {}] ", demandeDTO);

        String dateCommande = commandeDTO.getDateCreation().format(dateFormatter);
        String dateCapture = LocalDateTime.now().format(dateTimeFormatter);
        Operation<String> operation = new Operation<String>() {
            @Override
            public void execute() throws Exception {

                String montant = commandeDTO.getMontantInitial() + paiementPropertiesResolver.getCurrency();
                String montantACapturer = commandeOperationDTO.getMontant() + paiementPropertiesResolver.getCurrency();
                String montantDejaCapture = commandeDTO.getMontantDejaCapture() + paiementPropertiesResolver.getCurrency();
                String montantRestant = (commandeDTO.getMontantRestant() - commandeOperationDTO.getMontant()) + paiementPropertiesResolver.getCurrency();
                String version = paiementPropertiesResolver.getVersionCapture();
                MoyenPaiementDTO moyenPaiementDTO = commandeDTO.getMoyenPaiement();
                LOGGER.info("Paramètres Capture:\nURL: {}\nTPE: {}\nmontant: {}\nmontant_a_capturer: {}\nmontant_deja_capture: {}\nmontant_restant: {}\nlgue: {}\nreference: {}\ndate (date de la capture): {}\ndate_commande: {}\nsociete: {}\nversion {}",
                        paiementPropertiesResolver.getCaptureUrl(), getTpe(), montant, montantACapturer, montantDejaCapture, montantRestant, moyenPaiementDTO.getLangue(),
                        moyenPaiementDTO.getPkMoyenPaiements(), dateCapture, dateCommande, moyenPaiementDTO.getCodeSociete(), version);

                // Permet de désactiver la capture en simulant une erreur d'opération.
                PropertiesDTO captureActive = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_ACTIVATION_CAPTURE_PAIEMENT);
                LOGGER.info("capture_active: {}", captureActive != null ? captureActive.getValue() : "true");
                if (captureActive != null && !Boolean.parseBoolean(captureActive.getValue())) {
                    // On met le statut 400 pour éviter de faire plusieurs tentatives
                    throw new HttpResponseException(Response.Status.BAD_REQUEST.getStatusCode(), "Capture du paiement désactivé");
                }

                Response response = getTarget().queryParam("TPE", getTpe())
                        .queryParam("montant", montant)
                        .queryParam("montant_a_capturer", montantACapturer)
                        .queryParam("montant_deja_capture", montantDejaCapture)
                        .queryParam("montant_restant", montantRestant)
                        .queryParam("lgue", moyenPaiementDTO.getLangue())
                        .queryParam("reference", moyenPaiementDTO.getPkMoyenPaiements())
                        .queryParam("date", dateCapture)
                        .queryParam("date_commande", dateCommande)
                        .queryParam("societe", moyenPaiementDTO.getCodeSociete())
                        .queryParam("version", version)
                        .request(MediaType.APPLICATION_JSON).get();

                String responseString = response.readEntity(String.class);
                LOGGER.info("Capture [ responseString {}] ", responseString);
                setResult(responseString);
                extractResult(responseString, commandeOperationDTO);

                if (!OperationStatutEnum.ACCEPTEE.name().equals(commandeOperationDTO.getOperationStatut())) {
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
                if (OperationStatutEnum.ERREUR.name().equals(commandeOperationDTO.getOperationStatut())) {
                    sendMail(demandeDTO, operation, 4);
                } else {
                    commandeOperationDTO.setOperationStatut(OperationStatutEnum.ERREUR.name());
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
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE);

        if (propertiesDTO.getValue() != null) {
            EmailInfoDTO emailInfo = new EmailInfoDTO();
            emailInfo.setLangue("fr");
            emailInfo.setBodyTemplateCode(bodyTemplateCode);
            emailInfo.setSubjectTemplateCode(subjectTemplateCode);
            emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(), afBackUtils.getDemarcheInfos().getEmailFromNom());
            emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(), afBackUtils.getDemarcheInfos().getEmailReplytoNom());
            emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, demandeDTO.getIdentifiant());

            String[] adresses = propertiesDTO.getValue().trim().split(",");
            for (String adresseMail : adresses) {
                emailInfo.addTo(adresseMail, "Support Technique");
            }

            Map<String, Object> model = new HashMap<>();
            model.put("incident", incident);
            model.put("dateTimeString", dateTimeString);
            model.put("Pkdemandes", demandeDTO.getPkDemandes());
            model.put("resultat", operation == null ? null : operation.getResult());
            try {
                mailService.sendMail(emailInfo, model);
            } catch (Exception e) {
                LOGGER.error("Erreur lors de l'envoi de l'email", e);
            }
        }
    }

    private void extractResult(String responseString, CommandeOperationDTO operation) {
        for (String s : responseString.split("\n")) {
            String[] keyValue = s.split("=");

            switch (keyValue[0]) {
                case "cdr": // cdr = Code retour indiquant le résultat de la capture
                    operation.setCodeRetour(keyValue[1]);
                    if ("1".equals(keyValue[1])) {
                        operation.setOperationStatut(OperationStatutEnum.ACCEPTEE.name());
                    } else if ("0".equals(keyValue[1])) {
                        operation.setOperationStatut(OperationStatutEnum.REFUSEE.name());
                    } else {
                        operation.setOperationStatut(OperationStatutEnum.ERREUR.name());
                    }
                    break;
                case "aut": // aut = Numéro d’autorisation du paiement si celui-ci a été accepté
                    operation.setNumeroAutorisation(Integer.parseInt(keyValue[1]));
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

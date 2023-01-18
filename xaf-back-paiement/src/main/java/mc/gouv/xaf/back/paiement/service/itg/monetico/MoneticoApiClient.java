package mc.gouv.xaf.back.paiement.service.itg.monetico;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.retry.Operation;
import mc.gouv.xaf.back.paiement.retry.OperationHelper;
import mc.gouv.xaf.back.paiement.service.itg.PaiementApiClient;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.enums.MailSupportEnum;
import org.apache.commons.lang3.StringUtils;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Component
public class MoneticoApiClient implements PaiementApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoneticoApiClient.class);
    private final WebTarget target;
    private final String tpe;
    private final PaiementPropertiesResolver paiementPropertiesResolver;
    private final OperationHelper operationHelper;
    private final MailService mailService;
    private final PropertiesService propertiesService;
    private final GouvPropertiesResolver gouvPropertiesResolver;

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy:HH:mm:ss");

    public MoneticoApiClient(Proxy proxy,
                             PaiementPropertiesResolver paiementPropertiesResolver,
                             OperationHelper operationHelper,
                             MailService mailService,
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
        this.propertiesService = propertiesService;
        this.gouvPropertiesResolver = gouvPropertiesResolver;
    }

    public boolean capture(CommandeDTO commandeDTO, CommandeOperationDTO commandeOperationDTO, DemandeDTO demandeDTO) {
        logStartMethod(LOGGER);

        LOGGER.info("Parameters [ CommandeDTO {}] ", commandeDTO);
        LOGGER.info("Parameters [ OperationBO {}] ", commandeOperationDTO);
        LOGGER.info("Parameters [ DemandeDTO {}] ", demandeDTO);

        Operation<String> operation = buildOperation(commandeDTO, commandeOperationDTO);

        try {
            operationHelper.executeWithRetry(operation);
        } catch (Exception exception) {
            LOGGER.error("Impossible de faire la capture");
            LOGGER.error(exception.getMessage(), exception);

            if (mailService != null) {
                if (OperationStatutEnum.ERREUR.name().equals(commandeOperationDTO.getOperationStatut())) {
                    sendMail(demandeDTO, operation, 4);
                } else {
                    commandeOperationDTO.setOperationStatut(OperationStatutEnum.ERREUR.name());
                    sendMail(demandeDTO, operation, 3);
                }
            }
        }

        return StringUtils.equals(commandeOperationDTO.getOperationStatut(), OperationStatutEnum.ACCEPTEE.name());
    }

    private void sendMail(DemandeDTO demandeDTO, Operation<?> operation, int incident) {
        String bodyTemplateCode = "MAIL_CAPTURE_ECHEC_CORPS";
        String subjectTemplateCode = "MAIL_CAPTURE_ECHEC_OBJET";
        Set<String> mailingLists = mailService.getMailingLists(MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE.name());
        Map<String, Object> model = new HashMap<>();
        model.put("resultat", operation == null ? null : operation.getResult());
        mailService.sendMailSupport(subjectTemplateCode, bodyTemplateCode, mailingLists, demandeDTO.getPkDemandes(), demandeDTO.getIdentifiant(), incident, model, null);
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
                    operation.setNumeroAutorisation(keyValue[1]);
                    break;
                case "lib": // lib = Libellé détaillé précisant la nature du code retour
                    operation.setLibelle(keyValue[1]);
                    break;
                default:
                    LOGGER.info("Clé de paramètre inconnue : {}", keyValue[0]);
            }
        }
    }

    public WebTarget getTarget() {
        return target;
    }

    public String getTpe() {
        return tpe;
    }

    private Operation<String> buildOperation(CommandeDTO commandeDTO, CommandeOperationDTO commandeOperationDTO) {
        String dateCommande = commandeDTO.getDateCreation().format(dateFormatter);
        String dateCapture = LocalDateTime.now().format(dateTimeFormatter);
        return new Operation<String>() {
            @Override
            public void execute() throws HttpResponseException {
                String montant = commandeDTO.getMontantInitial() + paiementPropertiesResolver.getCurrency();
                String montantACapturer = commandeOperationDTO.getMontant() + paiementPropertiesResolver.getCurrency();
                String montantDejaCapture = commandeDTO.getMontantDejaCapture() + paiementPropertiesResolver.getCurrency();
                String montantRestant = (commandeDTO.getMontantRestant() - commandeOperationDTO.getMontant()) + paiementPropertiesResolver.getCurrency();
                String version = paiementPropertiesResolver.getVersionCapture();
                MoyenPaiementDTO moyenPaiementDTO = commandeDTO.getMoyenPaiement();
                LOGGER.info("Paramètres Capture:\nURL: {}\nTPE: {}\nmontant: {}\nmontant_a_capturer: {}\nmontant_deja_capture: {}\nmontant_restant: {}\nlgue: {}\nreference: {}\ndate (date de la capture): {}\ndate_commande: {}\nsociete: {}\nversion {}",
                        paiementPropertiesResolver.getCaptureUrl(), getTpe(), montant, montantACapturer, montantDejaCapture, montantRestant, moyenPaiementDTO.getLangue(),
                        moyenPaiementDTO.getPkMoyenPaiements(), dateCapture, dateCommande, moyenPaiementDTO.getCodeSociete(), version);

                // Permet de désactiver la capture en simulant monetico injoignable
                PropertiesDTO errorProp = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), "TEMP_FAIL_CAPTURE_PAIEMENT_MONETICO_INJOIGNABLE");
                if (errorProp != null && "true".equals(errorProp.getValue()) ) {
                    // On met le statut 400 pour éviter de faire plusieurs tentatives
                    throw new HttpResponseException(Response.Status.BAD_REQUEST.getStatusCode(), "Capture du paiement désactivé");
                }

                // Permet de désactiver la capture en simulant un code retour 0
                PropertiesDTO errorProp2 = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), "TEMP_FAIL_CAPTURE_PAIEMENT_MONETICO_CODE_RETOUR");
                int statutCode;
                if ((errorProp2 != null && "true".equals(errorProp2.getValue()))) {
                    String responseString = "version=1.0\n" +
                            "reference=" + moyenPaiementDTO.getPkMoyenPaiements() + '\n' +
                            "cdr=0\n" +
                            "lib=autorisation refusee";
                    statutCode = 200;
                    LOGGER.info("Capture [ responseString {}] ", responseString);
                    setResult(responseString);
                    extractResult(responseString, commandeOperationDTO);
                } else {
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
                    statutCode = response.getStatus();
                    LOGGER.info("Capture [ responseString {}] ", responseString);
                    setResult(responseString);
                    extractResult(responseString, commandeOperationDTO);
                }

                String statut = commandeOperationDTO.getOperationStatut();
                if (StringUtils.equals(statut, OperationStatutEnum.ERREUR.name())
                        || StringUtils.equals(statut, OperationStatutEnum.INCIDENT.name())) {
                    throw new HttpResponseException(statutCode, "Operation non acceptee");
                }
            }

            @Override
            public Logger getLogger() {
                return LOGGER;
            }
        };
    }
}

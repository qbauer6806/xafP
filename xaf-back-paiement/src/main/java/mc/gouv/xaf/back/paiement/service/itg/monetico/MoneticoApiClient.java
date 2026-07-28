package mc.gouv.xaf.back.paiement.service.itg.monetico;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.dto.itg.monetico.CaptureDTO;
import mc.gouv.xaf.back.paiement.dto.itg.monetico.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.retry.Operation;
import mc.gouv.xaf.back.paiement.retry.OperationHelper;
import mc.gouv.xaf.back.paiement.service.itg.PaiementApiClient;
import mc.gouv.xaf.back.paiement.service.itg.PaiementSecurityService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.enums.MailSupportEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MoneticoApiClient implements PaiementApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoneticoApiClient.class);
    private static final String UN = "1";
    private static final String ZERO = "0";
    private static final String CDR = "cdr";
    private static final String AUT = "aut";
    private static final String LIB = "lib";
    @Getter
    private final RestClient restClient;
    @Getter
    private final String tpe;
    private final PaiementPropertiesResolver paiementPropertiesResolver;
    private final OperationHelper operationHelper;
    private final MailService mailService;
    private final PropertiesService propertiesService;
    private final PaiementSecurityService paiementSecurityService;

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy:HH:mm:ss");

    public MoneticoApiClient(PaiementPropertiesResolver paiementPropertiesResolver, OperationHelper operationHelper,
            MailService mailService, PropertiesService propertiesService,
            PaiementSecurityService paiementSecurityService) {

        this.restClient = RestClient.builder().baseUrl(paiementPropertiesResolver.getCaptureUrl()).build();

        this.tpe = paiementPropertiesResolver.getTpe();
        this.paiementPropertiesResolver = paiementPropertiesResolver;
        this.operationHelper = operationHelper;
        this.mailService = mailService;
        this.propertiesService = propertiesService;
        this.paiementSecurityService = paiementSecurityService;
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
                } else if (OperationStatutEnum.INCIDENT.name().equals(commandeOperationDTO.getOperationStatut())) {
                    sendMail(demandeDTO, operation, 100);
                } else {
                    commandeOperationDTO.setOperationStatut(OperationStatutEnum.INCIDENT.name());
                    sendMail(demandeDTO, operation, 3);
                }
            }
        }

        return StringUtils.equals(commandeOperationDTO.getOperationStatut(), OperationStatutEnum.ACCEPTEE.name());
    }

    private void sendMail(DemandeDTO demandeDTO, Operation<?> operation, int incident) {
        String bodyTemplateCode = "MAIL_CAPTURE_ECHEC_CORPS";
        String subjectTemplateCode = "MAIL_CAPTURE_ECHEC_OBJET";
        Set<String> mailingLists = mailService.getMailingLists(
                MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE.name());
        Map<String, Object> model = new HashMap<>();
        model.put("resultat", operation == null ? null : operation.getResult());
        mailService.sendMailSupport(subjectTemplateCode, bodyTemplateCode, mailingLists, demandeDTO.getPkDemandes(),
                demandeDTO.getIdentifiant(), incident, model, null);
    }

    private void extractResult(String responseString, CommandeOperationDTO operation) {
        for (String s : responseString.split("\n")) {
            String[] keyValue = s.split("=");

            if (keyValue.length == 2) {
                switch (keyValue[0]) {
                    case CDR: // cdr = Code retour indiquant le résultat de la capture
                        // TODO operation.setCodeRetour(keyValue[1]);
                        if (UN.equals(keyValue[1])) {
                            operation.setOperationStatut(OperationStatutEnum.ACCEPTEE.name());
                        } else if (ZERO.equals(keyValue[1])) {
                            operation.setOperationStatut(OperationStatutEnum.REFUSEE.name());
                        } else {
                            operation.setOperationStatut(OperationStatutEnum.ERREUR.name());
                        }
                        break;
                    case AUT: // aut = Numéro d’autorisation du paiement si celui-ci a été accepté
                        // TODO operation.setNumeroAutorisation(keyValue[1]);
                        break;
                    case LIB: // lib = Libellé détaillé précisant la nature du code retour
                        // TODO operation.setLibelle(keyValue[1]);
                        break;
                    default:
                        LOGGER.info("Clé de paramètre inconnue : {}", keyValue[0]);
                }
            }
        }
        /* TODO if (StringUtils.equals(UN, operation.getCodeRetour()) && StringUtils.isBlank(
                operation.getNumeroAutorisation())) {
            operation.setOperationStatut(OperationStatutEnum.INCIDENT.name());
        }*/
    }

    private Operation<String> buildOperation(CommandeDTO commandeDTO, CommandeOperationDTO commandeOperationDTO) {
        return new Operation<>() {

            @Override
            public void execute() throws HttpResponseException {
                String currency = paiementPropertiesResolver.getCurrency();
                MoyenPaiementDTO moyenPaiementDTO = commandeDTO.getMoyenPaiement();
                CaptureDTO captureDTO = new CaptureDTO();
                captureDTO.setTpe(getTpe());
                captureDTO.setDate(LocalDateTime.now().format(dateTimeFormatter));
                captureDTO.setDateCommande(commandeDTO.getDateCreation().format(dateFormatter));
                // TODO captureDTO.setLgue(moyenPaiementDTO.getLangue());
                captureDTO.setMontant(commandeDTO.getMontantInitial() + currency);
                captureDTO.setMontantACapturer(commandeOperationDTO.getMontant() + currency);
                captureDTO.setMontantDejaCapture(commandeDTO.getMontantDejaCapture() + currency);
                BigDecimal montantRestant = BigDecimal.valueOf(commandeDTO.getMontantRestant());
                montantRestant = montantRestant.subtract(BigDecimal.valueOf(commandeOperationDTO.getMontant()));
                captureDTO.setMontantRestant(montantRestant + currency);
                // TODO captureDTO.setReference(moyenPaiementDTO.getPkMoyenPaiements());
                // TODO captureDTO.setSociete(moyenPaiementDTO.getCodeSociete());
                captureDTO.setVersion(paiementPropertiesResolver.getVersionCapture());

                LOGGER.info("Paramètres Capture:\nURL: {}\n{}", paiementPropertiesResolver.getCaptureUrl(), captureDTO);

                // Création d'une clé MAC
                String mac = paiementSecurityService.getHmacStringCapture(captureDTO);

                // Permet de désactiver la capture en simulant monetico injoignable
                PropertiesDTO errorProp = propertiesService.getProperty(
                        "TEMP_FAIL_CAPTURE_PAIEMENT_MONETICO_INJOIGNABLE");
                if (errorProp != null && "true".equals(errorProp.getValue())) {
                    // On met le statut 400 pour éviter de faire plusieurs tentatives
                    throw new HttpResponseException(HttpStatus.BAD_REQUEST.value(),
                            "Capture du paiement désactivé");
                }

                // Permet de désactiver la capture en simulant un code retour 0
                PropertiesDTO errorProp2 = propertiesService.getProperty(
                        "TEMP_FAIL_CAPTURE_PAIEMENT_MONETICO_CODE_RETOUR");
                int statutCode;
                String responseString;
                if ((errorProp2 != null && "true".equals(errorProp2.getValue()))) {
                    /*TODO responseString =
                            "version=1.0\n" + "reference=" + moyenPaiementDTO.getPkMoyenPaiements() + '\n' + "cdr=0\n"
                                    + "lib=autorisation refusee";*/
                    responseString =
                            "version=1.0\n" + "reference=cdr=0\n"
                                    + "lib=autorisation refusee";
                    statutCode = 200;
                } else {
                    Map<String, Object> result = getRestClient().get()
                            .uri(uriBuilder -> uriBuilder.queryParam("TPE", captureDTO.getTpe())
                                    .queryParam("date", captureDTO.getDate())
                                    .queryParam("date_commande", captureDTO.getDateCommande())
                                    .queryParam("lgue", captureDTO.getLgue())
                                    .queryParam("montant", captureDTO.getMontant())
                                    .queryParam("montant_a_capturer", captureDTO.getMontantACapturer())
                                    .queryParam("montant_deja_capture", captureDTO.getMontantDejaCapture())
                                    .queryParam("montant_restant", captureDTO.getMontantRestant())
                                    .queryParam("reference", captureDTO.getReference())
                                    .queryParam("societe", captureDTO.getSociete())
                                    .queryParam("version", captureDTO.getVersion()).queryParam("MAC", mac).build())
                            .exchange((request, response) -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put("status", response.getStatusCode().value());
                                map.put("body", new String(response.getBody().readAllBytes()));
                                return map;
                            });

                    responseString = (String) result.get("body");
                    statutCode = (Integer) result.get("status");
                }
                LOGGER.info("Capture [ responseString {}] ", responseString);
                setResult(responseString);
                extractResult(responseString, commandeOperationDTO);

                String statut = commandeOperationDTO.getOperationStatut();
                if (StringUtils.equals(statut, OperationStatutEnum.ERREUR.name()) || StringUtils.equals(statut,
                        OperationStatutEnum.INCIDENT.name()) || statutCode != 200) {
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

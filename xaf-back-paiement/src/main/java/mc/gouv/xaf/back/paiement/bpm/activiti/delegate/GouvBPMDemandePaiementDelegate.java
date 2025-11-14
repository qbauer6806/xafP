package mc.gouv.xaf.back.paiement.bpm.activiti.delegate;

import static mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum.ACCEPTEE;
import static mc.gouv.xaf.back.service.utils.AfBackUtils.DTF_AAAA_MM_JJ;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.itg.monetico.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.enums.PaiementStatutEnum;
import mc.gouv.xaf.back.paiement.service.CaptureService;
import mc.gouv.xaf.back.paiement.service.PaiementHistoriqueService;
import mc.gouv.xaf.back.paiement.service.data.CommandesService;
import mc.gouv.xaf.back.paiement.service.impl.TicketRecapitulatifServiceImpl;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.histo.DemandesHistoriqueService;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.dto.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.impl.AfMailTemplateModelProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.DemandeUsagerDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GouvBPMDemandePaiementDelegate implements JavaDelegate {

    public static final String MC_CAPTURE_RESULT = "MC_CAPTURE_RESULT";
    public static final String MC_FACTURE_REFERENCE = "MC_FACTURE_REFERENCE";
    public static final String MC_IS_DEBIT_KO = "MC_IS_DEBIT_KO";
    private static final String NB_JOURS_AVANT_EXPIRATION_PAIEMENT = "NB_JOURS_AVANT_EXPIRATION_PAIEMENT";

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMDemandePaiementDelegate.class);

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private CaptureService captureService;

    @Autowired
    private GouvBPM gouvBPM;

    @Autowired
    private TicketRecapitulatifServiceImpl ticketRecapitulatifService;

    @Autowired
    private DemandesDataService demandesDataService;

    @Autowired
    private PaiementHistoriqueService paiementHistoriqueService;

    @Autowired
    private DemandesHistoriqueService demandesHistoriqueService;

    @Autowired
    private MailService mailService;

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private CommandesService commandesService;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private AfMailTemplateModelProvider afMailTemplateModelProvider;

    @Override
    public void execute(DelegateExecution execution) {
        LOGGER.info("==== xaf-back-paiement CAPTURE PAIEMENT ...");

        Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());
        CommandeOperationDTO operation = null;
        CommandeDTO commandeDTO = null;
        DemandeDTO demandeDto = demandesService.getDemande(demandeId);
        DemandeDataDTO statutPaiementData = demandesDataService.getDemandeData(demandeId,
                PaiementDemandeDataKeysEnum.STATUT_PAIEMENT.name());

        try {
            commandeDTO = commandesService.getDerniereCommande(demandeId);
            LOGGER.info("Recuperation commandeDTO : {}", commandeDTO);
            LOGGER.info("Statut de l'empreinte de paiement : {}", statutPaiementData.getValue());
            if (commandeDTO != null && StringUtils.equals(statutPaiementData.getValue(),
                    PaiementStatutEnum.EMPREINTE_VALIDE.name())) {
                LOGGER.info("Début capture paiement pour la demande: {}", demandeId);
                operation = captureService.capture(commandeDTO, demandeDto);
                LOGGER.info("Fin capture paiement : {}", operation.getOperationStatut());
            }
        } catch (Exception e) {
            LOGGER.error("Erreur Capture paiement", e);
        }

        LOGGER.info("Mise à jour du statut du paiement et ajout de l'historique de paiement...");
        boolean resultatOperation = operation != null && ACCEPTEE.name().equals(operation.getOperationStatut());
        gouvBPM.setProcessBusinessVariable(demandeId, MC_CAPTURE_RESULT, resultatOperation);
        if (!resultatOperation) {
            if (StringUtils.equals(statutPaiementData.getValue(), PaiementStatutEnum.EMPREINTE_VALIDE.name())) {
                demandesDataService.saveOrUpdateDemandeData(demandeId,
                        PaiementDemandeDataKeysEnum.STATUT_PAIEMENT.name(), PaiementStatutEnum.DEBIT_ECHEC.name());
                paiementHistoriqueService.ajouterHistoriqueDebitEchec(demandeDto);
                // #43127 Envoi du mail débit en echec (MAIL_NOTIFICATION_DEMANDE_ECHEC_DEBIT_USAGER_CORPS)
                LOGGER.info("Début d'envoi du mail de débit en échec pour la demande {}...",
                        demandeDto.getIdentifiant());
                sendMail(demandeDto, "MAIL_NOTIFICATION_DEMANDE_ECHEC_DEBIT_USAGER");
            } else if (StringUtils.equals(statutPaiementData.getValue(), PaiementStatutEnum.EMPREINTE_EXPIREE.name())) {
                // #43127 Envoi du mail empreinte expirée (MAIL_NOTIFICATION_DEMANDE_EXPIRATION_EMPREINTE_USAGER_CORPS)
                LOGGER.info("Début d'envoi du mail d'expiration d'empreinte pour la demande {}...",
                        demandeDto.getIdentifiant());
                sendMail(demandeDto, "MAIL_NOTIFICATION_DEMANDE_EXPIRATION_EMPREINTE_USAGER");
            }
            // On ajoute un flag dans le BPMN pour savoir qu'un débit a déjà été émis
            gouvBPM.setProcessBusinessVariable(demandeId, MC_IS_DEBIT_KO, true);
            demandesHistoriqueService.actionSysteme(demandeId, "ECHEC", "Débit en échec. Demande de paiement envoyée");
        } else {
            demandesDataService.saveOrUpdateDemandeData(demandeId, PaiementDemandeDataKeysEnum.STATUT_PAIEMENT.name(),
                    PaiementStatutEnum.DEBIT_REALISE.name());
            demandesDataService.saveOrUpdateDemandeData(demandeId, PaiementDemandeDataKeysEnum.DATE_PAIEMENT.name(),
                    LocalDateTime.now().format(DTF_AAAA_MM_JJ));
            demandesDataService.saveOrUpdateDemandeData(demandeId, PaiementDemandeDataKeysEnum.MONTANT_PAYE.name(),
                    operation.getMontant().toString());
            paiementHistoriqueService.ajouterHistoriqueDebitOK(demandeDto);
            // On récupère le flag pour l'historique
            if (BooleanUtils.isTrue((Boolean) gouvBPM.getProcessBusinessVariables(demandeId).get(MC_IS_DEBIT_KO))) {
                demandesHistoriqueService.actionUsager(demandeId, demandeDto.getUsagerId(), "SUCCES", "Paie en ligne");
            } else {
                demandesHistoriqueService.actionSysteme(demandeId, "SUCCES", "Débit réalisé avec succès");
            }
            //LOGGER.info("Recuperation reference : {}", operation.getNumeroFacture());
            ticketRecapitulatifService.sendMail(operation, commandeDTO, demandeId);
            //gouvBPM.setProcessBusinessVariable(demandeId, MC_FACTURE_REFERENCE, operation.getNumeroFacture());
        }
        LOGGER.info("==== xaf-back-paiement CAPTURE PAIEMENT <fin>");
    }

    private void sendMail(DemandeDTO demandeDTO, String mailKey) {
        String bodyTemplateCode = mailKey + "_CORPS";
        String subjectTemplateCode = mailKey + "_OBJET";
        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setLangue(demandeDTO.getLangue());
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(),
                afBackUtils.getDemarcheInfos().getEmailFromNom());
        DemandeUsagerDTO usager = demandeDTO.getUsager();
        if (usager != null) {
            emailInfo.addTo(usager.getEmail(), usager.getPrenom() + " " + usager.getNom());
        }
        emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, demandeDTO.getIdentifiant());
        Map<String, Object> model = afMailTemplateModelProvider.getGenericModelDemande(demandeDTO);

        // Calcul de la date expiration de la demande avec valeur par défaut à 35 jours
        PropertiesDTO prop = propertiesService.getProperty(NB_JOURS_AVANT_EXPIRATION_PAIEMENT);
        int nbJoursAvantExpiration = (null != prop) ? Integer.parseInt(prop.getValue()) : 35;
        model.put("dateExpirationPaiement", LocalDate.now().plusDays(nbJoursAvantExpiration)
                .format(DateTimeFormatter.ofPattern(AfBackUtils.DEFAULT_FRENCH_DATE_FORMAT)));
        try {
            mailService.sendMail(emailInfo, model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }
    }
}

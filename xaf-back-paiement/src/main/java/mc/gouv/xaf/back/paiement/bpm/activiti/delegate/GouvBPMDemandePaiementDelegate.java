package mc.gouv.xaf.back.paiement.bpm.activiti.delegate;

import static mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum.ACCEPTEE;
import static mc.gouv.xaf.back.service.utils.AfBackUtils.DTF_AAAA_MM_JJ;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.enums.PaiementStatutEnum;
import mc.gouv.xaf.back.paiement.service.CaptureService;
import mc.gouv.xaf.back.paiement.service.PaiementHistoriqueService;
import mc.gouv.xaf.back.paiement.service.data.CommandesService;
import mc.gouv.xaf.back.paiement.service.impl.TicketRecapitulatifServiceImpl;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.AfHistoService;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;

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
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private TicketRecapitulatifServiceImpl ticketRecapitulatifService;

    @Autowired
    private DemandesDataService demandesDataService;

    @Autowired
    private PaiementHistoriqueService paiementHistoriqueService;

    @Autowired
    private AfHistoService histoService;
    
    @Autowired
    private MailService mailService;
    
    @Autowired
    private AfBackUtils afBackUtils;
    
    @Autowired
    private UsagersCache usagersCache;

    @Autowired
    private CommandesService commandesService;
    
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private PropertiesService propertiesService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("==== xaf-back-paiement CAPTURE PAIEMENT ...");

        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());
        CommandeOperationDTO operation = null;
        CommandeDTO commandeDTO = null;
        DemandeDTO demandeDto = demandesService.getDemande(demarcheId, demandeId);
        DemandeDataDTO statutPaiementData = demandesDataService.getDemandeData(demarcheId, demandeId, PaiementDemandeDataKeysEnum.STATUT_PAIEMENT.name());

        try {
            commandeDTO = commandesService.getDerniereCommande(demandeId);
            LOGGER.info("Recuperation commandeDTO : {}", commandeDTO);
            LOGGER.info("Statut de l'empreinte de paiement : {}", statutPaiementData.getValue());
            if (commandeDTO != null && StringUtils.equals(statutPaiementData.getValue(), PaiementStatutEnum.EMPREINTE_VALIDE.name())) {
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
                demandesDataService.saveOrUpdateDemandeData(demarcheId, demandeId, PaiementDemandeDataKeysEnum.STATUT_PAIEMENT.name(), PaiementStatutEnum.DEBIT_ECHEC.name());
                paiementHistoriqueService.ajouterHistoriqueDebitEchec(demandeDto);
                // #43127 Envoi du mail débit en echec (MAIL_NOTIFICATION_DEMANDE_ECHEC_DEBIT_USAGER_CORPS)
                sendMail(demandeDto, "MAIL_NOTIFICATION_DEMANDE_ECHEC_DEBIT_USAGER");
            } else if (StringUtils.equals(statutPaiementData.getValue(), PaiementStatutEnum.EMPREINTE_EXPIREE.name())) {
            	// #43127 Envoi du mail empreinte expirée (MAIL_NOTIFICATION_DEMANDE_EXPIRATION_EMPREINTE_USAGER_CORPS)
            	sendMail(demandeDto, "MAIL_NOTIFICATION_DEMANDE_EXPIRATION_EMPREINTE_USAGER");
            }
            // On ajoute un flag dans le BPMN pour savoir qu'un débit a déjà été émis
            gouvBPM.setProcessBusinessVariable(demandeId, MC_IS_DEBIT_KO, true);
            histoService.actionSysteme(demandeId, "ECHEC", "Débit en échec. Demande de paiement envoyée");
        } else {
            // TODO sauvegarder le statut du paiement de façon plus correct que dans les demandes data
            demandesDataService.saveOrUpdateDemandeData(demarcheId, demandeId, PaiementDemandeDataKeysEnum.STATUT_PAIEMENT.name(), PaiementStatutEnum.DEBIT_REALISE.name());
            demandesDataService.saveOrUpdateDemandeData(demarcheId, demandeId, PaiementDemandeDataKeysEnum.DATE_PAIEMENT.name(), LocalDateTime.now().format(DTF_AAAA_MM_JJ));
            demandesDataService.saveOrUpdateDemandeData(demarcheId, demandeId, PaiementDemandeDataKeysEnum.MONTANT_PAYE.name(), operation.getMontant().toString());
            paiementHistoriqueService.ajouterHistoriqueDebitOK(demandeDto);
            // On récupère le flag pour l'historique
            if (BooleanUtils.isTrue((Boolean) gouvBPM.getProcessBusinessVariables(demandeId).get(MC_IS_DEBIT_KO))) {
                histoService.actionUsager(demandeId, demandeDto.getUsagerId(), "SUCCES", "Paie en ligne");
            } else {
                histoService.actionSysteme(demandeId, "SUCCES", "Débit réalisé avec succès");
            }
            LOGGER.info("Recuperation reference : {}", operation.getNumeroFacture());
            ticketRecapitulatifService.sendMail(operation, commandeDTO, demandeId);
            gouvBPM.setProcessBusinessVariable(demandeId, MC_FACTURE_REFERENCE, operation.getNumeroFacture());
        }
        LOGGER.info("==== xaf-back-paiement CAPTURE PAIEMENT <fin>");
    }
    
	private void sendMail(DemandeDTO demandeDTO, String mailKey) {
		String bodyTemplateCode = mailKey + "_CORPS";
		String subjectTemplateCode = mailKey + "_OBJET";
		GichuniUsagerDTO usager = usagersCache.get(demandeDTO.getUsagerId(), true);
		if (usager == null) {
			usager = new GichuniUsagerDTO();
			usager.setNom(demandeDTO.getUsagerNom());
			usager.setPrenom(demandeDTO.getUsagerPrenom());
			usager.setEmail(demandeDTO.getUsagerEmail());
		}
		EmailInfoDTO emailInfo = new EmailInfoDTO();
		emailInfo.setLangue(demandeDTO.getLangue());
		emailInfo.setBodyTemplateCode(bodyTemplateCode);
		emailInfo.setSubjectTemplateCode(subjectTemplateCode);
		emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(),
				afBackUtils.getDemarcheInfos().getEmailFromNom());
		emailInfo.addTo(demandeDTO.getUsagerEmail(), demandeDTO.getUsagerPrenom() + " " + demandeDTO.getUsagerNom());
		emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, demandeDTO.getIdentifiant());
		Map<String, Object> model = new HashMap<>();
		String defaultMailTitre = demandeDTO.getLangue().equals("fr") ? SharedMessages.DEFAULT_TITRE_MAIL_FR
				: SharedMessages.DEFAULT_TITRE_MAIL_EN;
		String titre = usager.getTitre() != null
				? messageSource.getMessage("civilite." + usager.getTitre(), null, new Locale(demandeDTO.getLangue()))
				: defaultMailTitre;
		model.put("titre", titre);
		model.put("urlFront", gouvPropertiesResolver.getFrontUrl());
		model.put("identifiant", demandeDTO.getIdentifiant());
		model.put("pkDemande", demandeDTO.getPkDemandes());

		// Calcul de la date d'expoiration de la demande avec valeur par défaut à 35
		// jours
		PropertiesDTO prop = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(),
				NB_JOURS_AVANT_EXPIRATION_PAIEMENT);
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

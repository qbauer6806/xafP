package mc.gouv.xaf.back.service.relance;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.relance.settings.RelanceStatutDemandeConf;
import mc.gouv.xaf.back.service.utils.RelancesUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeDTO;

@Service
@EnableScheduling
public class RelancesDemandesServiceImpl implements RelancesDemandesService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RelancesDemandesServiceImpl.class);
	
	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Autowired
	private MailService mailService;
	
	@Autowired
	private RelancesUtils relanceUtils;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private UsagersCache usagersCache;
	
	@Override
	public void sendRelancesMail(List<RelanceStatutDemandeConf> statutsARelancer) {
		// On recupère toutes les demandes du TS appelant qui feront l'objet d'une relance
		Map<DemandeDTO, String> demandesANotifier = relanceUtils.getDemandesANotifier(statutsARelancer);
		for (Map.Entry<DemandeDTO, String> entry : demandesANotifier.entrySet()) {
			LOGGER.info("Début du processus de relance des demandes...");
			envoiEmailUsagerRelance(entry.getKey(), entry.getValue());
			relanceUtils.setRelanceDate(entry.getKey());
		}
	}

	@Override
	public void envoiEmailUsagerRelance(DemandeDTO demande, String codeMailPrefix) {
		final String subjectTemplateCode = codeMailPrefix + "_OBJET";
		final String bodyTemplateCode = codeMailPrefix + "_CORPS";

		GichuniUsagerDTO usager = usagersCache.get(demande.getUsagerId());
		if (usager == null) {
			usager = new GichuniUsagerDTO();
			usager.setNom(demande.getUsagerNom());
			usager.setPrenom(demande.getUsagerPrenom());
			usager.setEmail(demande.getUsagerEmail());
		}

		EmailInfoDTO emailInfoDTO = relanceUtils.creationMailUsager(bodyTemplateCode, subjectTemplateCode,
				demande.getLangue());
		String usagerNom = demande.getUsagerNom();
		String usagerPrenom = demande.getUsagerPrenom();
		emailInfoDTO.addTo(demande.getUsagerEmail(), usagerPrenom + " " + usagerNom);
		Map<String, Object> model = new HashMap<>();
		model.put("identifiant", demande.getIdentifiant());
		model.put("expireDans", relanceUtils.getExpirationTime(demande));
		model.put("pkDemande", demande.getPkDemandes());
		model.put("urlFront", gouvPropertiesResolver.getFrontUrl());
		String defaultMailTitre = demande.getLangue().equals("fr") ? SharedMessages.DEFAULT_TITRE_MAIL_FR
				: SharedMessages.DEFAULT_TITRE_MAIL_EN;
		String titre = usager.getTitre() != null
				? messageSource.getMessage("civilite." + usager.getTitre(), null, new Locale(demande.getLangue()))
				: defaultMailTitre;
		model.put("titre", titre);

		try {
			mailService.sendMail(emailInfoDTO, model);
		} catch (Exception e) {
			LOGGER.error("Erreur lors de l'envoi de l'email usager de relance pour la demande {}",
					demande.getIdentifiant());
		}
	}
}

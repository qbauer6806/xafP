package mc.gouv.xaf.back.service.relance;

import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.MailTemplateModelProvider;
import mc.gouv.xaf.back.service.relance.settings.RelanceStatutDemandeConf;
import mc.gouv.xaf.back.service.utils.RelancesUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@EnableScheduling
public class RelancesDemandesServiceImpl implements RelancesDemandesService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RelancesDemandesServiceImpl.class);

	@Autowired
	private MailService mailService;
	
	@Autowired
	private RelancesUtils relanceUtils;


	@Autowired
	private MailTemplateModelProvider mailTemplateModelProvider;
	
	@Override
	public void sendRelancesMail(List<RelanceStatutDemandeConf> statutsARelancer) {
		// On recupère toutes les demandes du TS appelant qui feront l'objet d'une relance
		Map<DemandeDTO, String> demandesANotifier = relanceUtils.getDemandesANotifier(statutsARelancer);
		for (Map.Entry<DemandeDTO, String> entry : demandesANotifier.entrySet()) {
			envoiEmailUsagerRelance(entry.getKey(), entry.getValue());
			relanceUtils.setRelanceDate(entry.getKey());
		}
	}

	@Override
	public void envoiEmailUsagerRelance(DemandeDTO demande, String codeMailPrefix) {
		final String subjectTemplateCode = codeMailPrefix + "_OBJET";
		final String bodyTemplateCode = codeMailPrefix + "_CORPS";

		EmailInfoDTO emailInfoDTO = relanceUtils.creationMailUsager(bodyTemplateCode, subjectTemplateCode, demande.getLangue());
		String usagerNom = demande.getUsagerNom();
		String usagerPrenom = demande.getUsagerPrenom();
		emailInfoDTO.addTo(demande.getUsagerEmail(), usagerPrenom + " " + usagerNom);
		Map<String,Object> model = mailTemplateModelProvider.getGenericModelDemande(demande);
		model.put("expireDans", relanceUtils.getExpirationTime(demande));

		try {
			mailService.sendMail(emailInfoDTO, model);
		} catch (Exception e) {
			LOGGER.error("Erreur lors de l'envoi de l'email usager de relance pour la demande {}",
					demande.getIdentifiant());
		}
	}
}

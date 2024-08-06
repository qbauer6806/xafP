package mc.gouv.xaf.back.bpm.activiti.delegate;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.MailTemplateModelProvider;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeUsagerDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Classe service appelée par le process Activiti pour envoyer un email à l'usager.
 * 
 * @author qdeme
 *
 */
@Component
public class GouvBPMEnvoiEmailUsagerDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMEnvoiEmailUsagerDelegate.class);

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private UsagersCache usagerCache;

    @Autowired
    private MailService mailService;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private MailTemplateModelProvider mailTemplateModelProvider;

    @Setter
    @Getter
    private Expression emailBodyTemplateCode;

    @Setter
    @Getter
    private Expression emailSubjectTemplateCode;
    
    private Expression copieCacheeAuService;

    @Override
    public void execute(DelegateExecution execution) {

		LOGGER.info("==== xaf-back ENVOI EMAIL USAGER ...");
		Integer usagerId = (Integer) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_USAGERID.name());
		GichuniUsagerDTO usager = usagerCache.get(usagerId, true);
		Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());
		DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);
		if (usager == null) {
            usager = new GichuniUsagerDTO();
            DemandeUsagerDTO usagerDto = demande.getUsager();
            if (usagerDto != null) {
                usager.setNom(usagerDto.getNom());
                usager.setPrenom(usagerDto.getPrenom());
                usager.setEmail(usagerDto.getEmail());
            }
		}

		String bodyTemplateCode = (String) emailBodyTemplateCode.getValue(execution);
		String subjectTemplateCode = (String) emailSubjectTemplateCode.getValue(execution);
		String copieCacheeAuServiceStr = null;
		if (copieCacheeAuService != null) {
			copieCacheeAuServiceStr = (String) copieCacheeAuService.getValue(execution);
		}

		String langue = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_LANGUE.name());
		EmailInfoDTO emailInfo = new EmailInfoDTO();
		emailInfo.setBodyTemplateCode(bodyTemplateCode);
		emailInfo.setSubjectTemplateCode(subjectTemplateCode);
		emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(),
				afBackUtils.getDemarcheInfos().getEmailFromNom());
		emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(),
				afBackUtils.getDemarcheInfos().getEmailReplytoNom());

		String prenom = StringUtils.EMPTY;
		String nom = StringUtils.EMPTY;

		if (StringUtils.isNotBlank(usager.getPrenom())) {
			prenom = usager.getPrenom();
		}

		if (StringUtils.isNotBlank(usager.getNom())) {
			nom = usager.getNom();
		}

        emailInfo.addTo(usager.getEmail(), prenom + " " + nom);
        emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, execution.getProcessInstanceBusinessKey());
        emailInfo.setLangue(langue);
        
        if ("true".equals(copieCacheeAuServiceStr)) {
        	LOGGER.info("Paramètre \"copieCacheeAuService\" spécifié, placer le service en copie carbone invisible...");
        	emailInfo.addBcc(afBackUtils.getDemarcheInfos().getEmailService(), StringUtils.EMPTY);
        }

		String codeMotif = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());
		String commentaire = (String) execution
				.getVariable(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());
		commentaire = mailService.formatCommentaire(commentaire);
		Map<String, Object> model = mailTemplateModelProvider.getModel(subjectTemplateCode, bodyTemplateCode, demande,
				execution.getVariables(), codeMotif, commentaire);

		try {
			mailService.sendMail(emailInfo, model);
		} catch (Exception e) {
			LOGGER.error("Échec lors de l'envoi de l'email", e);
		}

		LOGGER.info("==== xaf-back ENVOI EMAIL USAGER <fin>");
	}

}

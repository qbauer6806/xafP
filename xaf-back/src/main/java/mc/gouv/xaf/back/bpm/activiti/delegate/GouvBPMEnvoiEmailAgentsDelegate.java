package mc.gouv.xaf.back.bpm.activiti.delegate;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.impl.AfMailTemplateModelProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.enums.MailAudienceEnum;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Classe service appelée par le process Activiti pour envoyer un email aux agents.
 *
 * @author qdeme
 */
@Component
public class GouvBPMEnvoiEmailAgentsDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMEnvoiEmailAgentsDelegate.class);

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private MailService mailService;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private AfMailTemplateModelProvider afMailTemplateModelProvider;

    @Setter
    @Getter
    private Expression emailBodyTemplateCode;

    @Setter
    @Getter
    private Expression emailSubjectTemplateCode;

    @Setter
    @Getter
    private Expression emailTemplateCode;

    @Override
    public void execute(DelegateExecution execution) {

        LOGGER.info("==== xaf-back ENVOI EMAIL AGENTS ...");

        String bodyTemplateCode = mailService.getEmailBodyTemplate(emailBodyTemplateCode, emailTemplateCode, execution);
        String subjectTemplateCode = mailService.getEmailSubjectTemplate(emailSubjectTemplateCode, emailTemplateCode,
                execution);

        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(),
                afBackUtils.getDemarcheInfos().getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(),
                afBackUtils.getDemarcheInfos().getEmailReplytoNom());
        emailInfo.addTo(afBackUtils.getDemarcheInfos().getEmailService(), StringUtils.EMPTY);
        emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, execution.getProcessInstanceBusinessKey());
        emailInfo.setLangue("fr");

        String codeMotif = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());
        String commentaire = (String) execution.getVariable(
                GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());

        Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());
        DemandeDTO demande = demandesService.getDemande(demandeId);

        Map<String, Object> model = afMailTemplateModelProvider.getModel(subjectTemplateCode, bodyTemplateCode, demande,
                execution.getVariables(), codeMotif, commentaire);

        try {
            mailService.sendMail(emailInfo, model, MailAudienceEnum.AGENT);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }

        LOGGER.info("==== xaf-back ENVOI EMAIL AGENTS <fin>");
    }

}

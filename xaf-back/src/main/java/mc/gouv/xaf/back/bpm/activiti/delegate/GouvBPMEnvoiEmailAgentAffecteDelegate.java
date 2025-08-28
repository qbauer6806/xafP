package mc.gouv.xaf.back.bpm.activiti.delegate;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.dto.EmailInfoDTO;
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
 * Classe service appelée par le process Activiti pour envoyer un email à l'agent affecté à la demande.
 *
 * @author qdeme
 */
@Component
public class GouvBPMEnvoiEmailAgentAffecteDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMEnvoiEmailAgentAffecteDelegate.class);

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private MailService mailService;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private UtilisateursCache utilisateursCache;

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

    private Expression copieAuService;

    @Override
    public void execute(DelegateExecution execution) {

        LOGGER.info("==== xaf-back ENVOI EMAIL AGENT AFFECTÉ ...");

        String bodyTemplateCode = mailService.getEmailBodyTemplate(emailBodyTemplateCode, emailTemplateCode, execution);
        String subjectTemplateCode = mailService.getEmailSubjectTemplate(emailSubjectTemplateCode, emailTemplateCode,
                execution);
        String copieAuServiceStr = null;
        if (copieAuService != null) {
            copieAuServiceStr = (String) copieAuService.getValue(execution);
        }

        LOGGER.info("bodyTemplateCode : {}", bodyTemplateCode);
        LOGGER.info("subjectTemplateCode : {}", subjectTemplateCode);

        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(),
                afBackUtils.getDemarcheInfos().getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(),
                afBackUtils.getDemarcheInfos().getEmailReplytoNom());

        // On récupère l'agent dans le bpmn parce qu'en cas de demande info compl, agent sera null dans demandeDto car il considère
        // que l'appel provient du front, et donc l'agent est caché pour raison de confidentialité
        Object agentIdObject = execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_ASSIGNEE.name());
        String agentId = agentIdObject != null ? (String) agentIdObject : null;
        User agent = utilisateursCache.get(agentId);
        if (agent != null) {
            LOGGER.info("Adresse / Nom de l'agent affecté à la demande : {} / {}", agent.getMail(), agent.getNom());
            emailInfo.addTo(agent.getMail(), agent.getNom());

            if ("true".equals(copieAuServiceStr)) {
                LOGGER.info("Paramètre \"copieAuService\" spécifié, placer le service en copie carbone...");
                emailInfo.addCc(afBackUtils.getDemarcheInfos().getEmailService(), StringUtils.EMPTY);
            }

            if (agent.getMail() != null) {
                emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, execution.getProcessInstanceBusinessKey());
                emailInfo.setLangue("fr");

                String codeMotif = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());
                String commentaire = (String) execution.getVariable(
                        GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());

                Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());
                DemandeDTO demande = demandesService.getDemande(demandeId);

                Map<String, Object> model = afMailTemplateModelProvider.getModel(subjectTemplateCode, bodyTemplateCode,
                        demande, execution.getVariables(), codeMotif, commentaire);

                try {
                    mailService.sendMail(emailInfo, model, MailAudienceEnum.AGENT);
                } catch (Exception e) {
                    LOGGER.error("Erreur lors de l'envoi de l'email", e);
                }
            } else {
                LOGGER.warn("Attention : l'utilisateur {} n'a pas d'adresse email associée. Pas d'envoi d'email.",
                        agent.getId());
            }
        }

        LOGGER.info("==== xaf-back ENVOI EMAIL AGENT AFFECTÉ <fin>");
    }

}

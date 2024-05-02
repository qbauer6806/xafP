package mc.gouv.xaf.back.bpm.activiti.delegate;

import java.util.Map;
import mc.gouv.logon.shared.User;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.MailTemplateModelProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.enums.MailAudienceEnum;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Classe service appelée par le process Activiti pour envoyer un email à l'agent affecté à la demande.
 * 
 * @author qdeme
 *
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
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @Autowired
    private MailTemplateModelProvider mailTemplateModelProvider;

    @Autowired
    private UtilisateursCache utilisateursCache;

    private Expression emailBodyTemplateCode;
    
    private Expression emailSubjectTemplateCode;
    
    private Expression copieAuService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        
        LOGGER.info("==== xaf-back ENVOI EMAIL AGENT AFFECTÉ ...");
        
        String bodyTemplateCode = (String)emailBodyTemplateCode.getValue(execution);
        String subjectTemplateCode = (String)emailSubjectTemplateCode.getValue(execution);
        String copieAuServiceStr = null;
        if (copieAuService != null) {
        	copieAuServiceStr = (String) copieAuService.getValue(execution);
        }
        
        LOGGER.info("bodyTemplateCode : {}", bodyTemplateCode);
        LOGGER.info("subjectTemplateCode : {}", subjectTemplateCode);
        
        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(), afBackUtils.getDemarcheInfos()
                .getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(), afBackUtils.getDemarcheInfos()
                .getEmailReplytoNom());

        // Récupérer l'adresse email de l'agent affecté à la demande
        String agentId = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_ASSIGNEE.name());
        User agent = utilisateursCache.get(agentId);
        LOGGER.info("Adresse / Nom de l'agent affecté à la demande : {} / {}", agent.getMail(), agent.getNom());
        emailInfo.addTo(agent.getMail(), agent.getNom());

        if ("true".equals(copieAuServiceStr)) {
        	LOGGER.info("Paramètre \"copieAuService\" spécifié, placer le service en copie carbone...");
        	emailInfo.addCc(afBackUtils.getDemarcheInfos().getEmailService(), StringUtils.EMPTY);
        }

        if (agent.getMail() != null) {
            emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, execution.getProcessBusinessKey());
            emailInfo.setLangue("fr");

            String codeMotif = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());
            String commentaire = (String) execution
                    .getVariable(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());

            Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());
            DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);

            Map<String,Object> model = mailTemplateModelProvider.getModel(subjectTemplateCode, bodyTemplateCode, demande, execution.getVariables(), codeMotif, commentaire);

            try {
                mailService.sendMail(emailInfo, model, MailAudienceEnum.AGENT);
            } catch (Exception e) {
                LOGGER.error("Erreur lors de l'envoi de l'email", e);
            }
        } else {
            LOGGER.warn("Attention : l'utilisateur {} n'a pas d'adresse email associée. Pas d'envoi d'email.", agent.getMatricule());
        }
        
        LOGGER.info("==== xaf-back ENVOI EMAIL AGENT AFFECTÉ <fin>");
    }

    public Expression getEmailBodyTemplateCode() {
        return emailBodyTemplateCode;
    }

    public void setEmailBodyTemplateCode(Expression emailBodyTemplateCode) {
        this.emailBodyTemplateCode = emailBodyTemplateCode;
    }

    public Expression getEmailSubjectTemplateCode() {
        return emailSubjectTemplateCode;
    }

    public void setEmailSubjectTemplateCode(Expression emailSubjectTemplateCode) {
        this.emailSubjectTemplateCode = emailSubjectTemplateCode;
    }

}

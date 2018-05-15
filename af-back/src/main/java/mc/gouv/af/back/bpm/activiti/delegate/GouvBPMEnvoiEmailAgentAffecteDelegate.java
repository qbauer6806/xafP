package mc.gouv.af.back.bpm.activiti.delegate;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.af.back.cache.UtilisateursCache;
import mc.gouv.af.back.mail.EmailInfoDTO;
import mc.gouv.af.back.mail.MailService;
import mc.gouv.af.back.mail.TemplateModelProvider;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.dem.service.DemandesService;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.logon.shared.User;

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
    private TemplateModelProvider templateModelProvider;
    
    @Autowired
    private UtilisateursCache utilisateursCache;
    
    private Expression emailBodyTemplateCode;
    
    private Expression emailSubjectTemplateCode;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        
        LOGGER.info("==== AF-BACK ENVOI EMAIL AGENT AFFECTÉ ...");
        
        String bodyTemplateCode = (String)emailBodyTemplateCode.getValue(execution);
        String subjectTemplateCode = (String)emailSubjectTemplateCode.getValue(execution);
        
        LOGGER.info("bodyTemplateCode : " + bodyTemplateCode);
        LOGGER.info("subjectTemplateCode : " + subjectTemplateCode);
        
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
        LOGGER.info("Adresse / Nom de l'agent affecté à la demande : " + agent.getMail() + " / " + agent.getNom());
        emailInfo.addTo(agent.getMail(), agent.getNom());
        
        if (agent.getMail() != null) {
            emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, execution.getProcessBusinessKey());
            emailInfo.setLangue("fr");
            
            String codeMotif = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());
            String commentaire = (String) execution
                    .getVariable(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());

            Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());
            DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);
            
            Map<String,Object> model = templateModelProvider.getModel(subjectTemplateCode, bodyTemplateCode, demande, execution.getVariables(), codeMotif, commentaire);
    
            try {
                mailService.sendMail(emailInfo, model);
            } catch (Exception e) {
                LOGGER.error("Erreur lors de l'envoi de l'email", e);
            }
        }
        else {
            LOGGER.warn("Attention : l'utilisateur " + agent.getMatricule() + " n'a pas d'adresse email associée. Pas d'envoi d'email.");
        }
        
        LOGGER.info("==== AF-BACK ENVOI EMAIL AGENT AFFECTÉ <fin>");
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

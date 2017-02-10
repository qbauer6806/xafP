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
import mc.gouv.af.back.mail.EmailInfoDTO;
import mc.gouv.af.back.mail.TemplateModelProvider;
import mc.gouv.af.back.mail.MailService;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.servicerest.usager.model.UsagerBean;

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
    private MailService mailService;
    
    @Autowired
    private TemplateModelProvider templateModelProvider;
    
    private Expression emailBodyTemplateCode;
    
    private Expression emailSubjectTemplateCode;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        
        LOGGER.info("==== AF-BACK ENVOI EMAIL USAGER ...");
        
        String bodyTemplateCode = (String)emailBodyTemplateCode.getValue(execution);
        String subjectTemplateCode = (String)emailSubjectTemplateCode.getValue(execution);
        
        Integer usagerId = (Integer)execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_USAGERID.name());
        String langue = (String)execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_LANGUE.name());
        
        UsagerBean usager = afBackUtils.getUsagerFromID(usagerId);
        
        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailService(), afBackUtils.getDemarcheInfos()
                .getEmailServiceNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(), afBackUtils.getDemarcheInfos()
                .getEmailReplytoNom());
        emailInfo.addTo(usager.getEmail(), usager.getPrenom() + " " + usager.getNom());
        emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, execution.getProcessBusinessKey());
        emailInfo.setLangue(langue);

        Map<String, Object> model = templateModelProvider.getModel(execution);

        try {
            mailService.sendMail(emailInfo, model);
        } catch (Exception e) {
            LOGGER.error("Échec lors de l'envoi de l'email", e);
        }
        
        LOGGER.info("==== AF-BACK ENVOI EMAIL USAGER <fin>");
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

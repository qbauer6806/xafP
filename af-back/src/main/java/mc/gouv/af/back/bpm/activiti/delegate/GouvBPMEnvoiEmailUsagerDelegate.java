package mc.gouv.af.back.bpm.activiti.delegate;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.af.back.cache.UsagersCache;
import mc.gouv.af.back.mail.EmailInfoDTO;
import mc.gouv.af.back.mail.MailService;
import mc.gouv.af.back.mail.MailTemplateModelProvider;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.dem.service.DemandesService;
import mc.gouv.dem.shared.model.DemandeDTO;
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
    private UsagersCache usagerCache;

    @Autowired
    private MailService mailService;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private MailTemplateModelProvider mailTemplateModelProvider;

    private Expression emailBodyTemplateCode;

    private Expression emailSubjectTemplateCode;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        LOGGER.info("==== AF-BACK ENVOI EMAIL USAGER ...");

        String bodyTemplateCode = (String) emailBodyTemplateCode.getValue(execution);
        String subjectTemplateCode = (String) emailSubjectTemplateCode.getValue(execution);

        Integer usagerId = (Integer) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_USAGERID.name());
        String langue = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_LANGUE.name());

        UsagerBean usager = usagerCache.get(usagerId);
        if (usager == null) {
            throw new Exception("Impossible d'envoyer un mail pour un usager inconnu : " + usagerId);
        }

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
        emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, execution.getProcessBusinessKey());
        emailInfo.setLangue(langue);

        String codeMotif = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());
        String commentaire = (String) execution
                .getVariable(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());

        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());
        DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);

        Map<String, Object> model = mailTemplateModelProvider.getModel(subjectTemplateCode, bodyTemplateCode, demande,
                execution.getVariables(), codeMotif, commentaire);

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

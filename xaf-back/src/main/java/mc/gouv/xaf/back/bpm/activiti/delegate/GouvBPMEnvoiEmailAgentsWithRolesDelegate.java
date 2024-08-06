package mc.gouv.xaf.back.bpm.activiti.delegate;

import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.shared.enums.MailAudienceEnum;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.common.engine.api.delegate.Expression;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.MailTemplateModelProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.back.service.itg.logon.dto.User;

/**
 * 
 * Classe service appelée par le process Activiti pour envoyer un email aux agents correspondant
 * aux rôles donnés en paramètre.
 * 
 * @author qdeme
 *
 */
@Component
public class GouvBPMEnvoiEmailAgentsWithRolesDelegate implements JavaDelegate {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMEnvoiEmailAgentsWithRolesDelegate.class);
    
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
    
    @Setter
    @Getter
    private Expression emailBodyTemplateCode;
    
    @Setter
    @Getter
    private Expression emailSubjectTemplateCode;
    
    private Expression roles;
    
    private Expression copieAuService;

    @Override
    public void execute(DelegateExecution execution) {
        
        LOGGER.info("==== xaf-back ENVOI EMAIL AGENT WITH ROLES ...");
        
        String bodyTemplateCode = (String)emailBodyTemplateCode.getValue(execution);
        String subjectTemplateCode = (String)emailSubjectTemplateCode.getValue(execution);
        String copieAuServiceStr = null;
        if (copieAuService != null) {
        	copieAuServiceStr = (String) copieAuService.getValue(execution);
        }
        
        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(), afBackUtils.getDemarcheInfos()
                .getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(), afBackUtils.getDemarcheInfos()
                .getEmailReplytoNom());
        
        LOGGER.info("Calcul des agents ayant les rôles requis pour l'envoi de l'email...");
        String rolesStr = (String)roles.getValue(execution);
        String[] rolesList = rolesStr.split(",");
        Set<User> destinataires = afBackUtils.getAgentsWithRoles(rolesList);
        if (destinataires != null && !destinataires.isEmpty()) {
            for (User dest : destinataires) {
                if (dest.getMail() != null) {
                    emailInfo.addTo(dest.getMail(), dest.getNom());
                }
                else {
                    LOGGER.warn("Attention : l'utilisateur {} n'a pas d'adresse email associée. Pas d'envoi d'email.", dest.getMatricule());
                }
            }
            LOGGER.info("Liste de destinataires calculée pour la liste de rôles [{}] : {}", rolesStr, emailInfo.getTo());
            
            emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, execution.getProcessInstanceBusinessKey());
            emailInfo.setLangue("fr");
            
            if ("true".equals(copieAuServiceStr)) {
            	LOGGER.info("Paramètre \"copieAuService\" spécifié, placer le service en copie carbone...");
            	emailInfo.addCc(afBackUtils.getDemarcheInfos().getEmailService(), StringUtils.EMPTY);
            }
            
            String codeMotif = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());
            String commentaire = (String) execution
                    .getVariable(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());

            Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());
            DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);
            
            Map<String,Object> model = mailTemplateModelProvider.getModel(subjectTemplateCode, bodyTemplateCode, demande, execution.getVariables(), codeMotif, commentaire);
    
            try {
                mailService.sendMail(emailInfo, model, MailAudienceEnum.AGENT);
            } catch (Exception e) {
                LOGGER.error("Erreur lors de l'envoi de l'email", e);
            }
        }
        else {
            LOGGER.warn("Attention : aucun agent n'a pu être retrouvé pour ces rôles. Par conséquent, pas d'envoi d'email.");
        }
        
        LOGGER.info("==== xaf-back ENVOI EMAIL AGENT WITH ROLES <fin>");
    }

}

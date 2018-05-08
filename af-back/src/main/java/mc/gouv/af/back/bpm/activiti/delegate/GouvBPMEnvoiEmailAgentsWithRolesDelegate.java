package mc.gouv.af.back.bpm.activiti.delegate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.af.back.mail.EmailInfoDTO;
import mc.gouv.af.back.mail.MailService;
import mc.gouv.af.back.mail.TemplateModelProvider;
import mc.gouv.af.back.service.properties.GouvPropertiesResolver;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.af.back.util.UtilisateursCache;
import mc.gouv.dem.service.DemandesService;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.logon.shared.Droit;
import mc.gouv.logon.shared.Role;
import mc.gouv.logon.shared.User;

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
    private TemplateModelProvider templateModelProvider;
    
    @Autowired
    private UtilisateursCache utilisateursCache;
    
    private Expression emailBodyTemplateCode;
    
    private Expression emailSubjectTemplateCode;
    
    private Expression roles;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        
        LOGGER.info("==== AF-BACK ENVOI EMAIL AGENT WITH ROLES ...");
        
        String bodyTemplateCode = (String)emailBodyTemplateCode.getValue(execution);
        String subjectTemplateCode = (String)emailSubjectTemplateCode.getValue(execution);
        
        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(), afBackUtils.getDemarcheInfos()
                .getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(), afBackUtils.getDemarcheInfos()
                .getEmailReplytoNom());
        
        LOGGER.info("Calcul des agents ayant les rôles requis pour l'envoi de l'email...");
        String codeAppli = gouvPropertiesResolver.getDemarcheId();
        Set<User> destinataires = new HashSet<User>();
        String rolesStr = (String)roles.getValue(execution);
        String[] rolesList = rolesStr.split(",");
        List<User> agents = new ArrayList<User>(utilisateursCache.getAll().values());
        if (agents != null && !agents.isEmpty()) {
            for (User agent : agents) {
                boolean toAdd = false;
                Set<Role> agentRoles = agent.getRoles();
                for (Role role : agentRoles) {
                    if (role.getAppli().getCode().equals(codeAppli)) {
                        for (Droit droit : role.getDroits()) {
                            for (String roleFromList : rolesList) {
                                if (roleFromList.trim().equals(droit.getCode())) {
                                    toAdd = true;
                                }
                            }
                        }
    
                    }
                }
                if (toAdd) {
                    destinataires.add(agent);
                }
            }
            
            for (User dest : destinataires) {
                if (dest.getMail() != null) {
                    emailInfo.addTo(dest.getMail(), dest.getNom());
                }
                else {
                    LOGGER.warn("Attention : l'utilisateur " + dest.getMatricule() + " n'a pas d'adresse email associée. Pas d'envoi d'email.");
                }
            }
            LOGGER.info("Liste de destinataires calculée pour la liste de rôles [" + rolesStr + "] : " + emailInfo.getTo());
            
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
            LOGGER.warn("Attention : aucun agent n'a pu être retrouvé. Par conséquent, pas d'envoi d'email.");
        }
        
        LOGGER.info("==== AF-BACK ENVOI EMAIL AGENT WITH ROLES <fin>");
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

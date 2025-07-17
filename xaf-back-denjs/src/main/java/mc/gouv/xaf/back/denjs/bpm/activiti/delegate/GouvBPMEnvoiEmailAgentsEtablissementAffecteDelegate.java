package mc.gouv.xaf.back.denjs.bpm.activiti.delegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.denjs.dto.DenjsAffectationAgentDTO;
import mc.gouv.xaf.back.denjs.service.DenjsAffectationService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
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
 * Classe service appelée par le process Activiti pour envoyer un email aux agents affectés à l'établissement à laquelle
 * est affectée la demande
 *
 * @author qdeme
 */
@Component
public class GouvBPMEnvoiEmailAgentsEtablissementAffecteDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            GouvBPMEnvoiEmailAgentsEtablissementAffecteDelegate.class);

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private MailService mailService;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private AfMailTemplateModelProvider afMailTemplateModelProvider;

    @Autowired
    private UtilisateursCache utilisateursCache;

    @Autowired
    private DenjsAffectationService denjsAffectationService;

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

        LOGGER.info("==== xaf-denjs ENVOI EMAIL AGENT DE L'ETABLISSEMENT AFFECTÉ ...");

        String bodyTemplateCode = mailService.getEmailBodyTemplate(emailBodyTemplateCode, emailTemplateCode, execution);
        String subjectTemplateCode = mailService.getEmailSubjectTemplate(emailSubjectTemplateCode, emailTemplateCode,
                execution);

        LOGGER.info("bodyTemplateCode : {}", bodyTemplateCode);
        LOGGER.info("subjectTemplateCode : {}", subjectTemplateCode);

        Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());

        List<String> matriculesDestinataires = new ArrayList<>();
        String etablissementCode = denjsAffectationService.getAffectationDemandeEtablissement(demandeId);
        if (etablissementCode != null) {
            List<DenjsAffectationAgentDTO> affectations = denjsAffectationService.getAffectationsAgents();
            for (DenjsAffectationAgentDTO affectation : affectations) {
                if (affectation.getEtablissementCode().equals(etablissementCode)) {
                    matriculesDestinataires.add(affectation.getAgentMatricule());
                }
            }
        }

        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(),
                afBackUtils.getDemarcheInfos().getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(),
                afBackUtils.getDemarcheInfos().getEmailReplytoNom());

        LOGGER.info("Liste de matricules destinataires de l'e-mail : {}", matriculesDestinataires);
        for (String matricule : matriculesDestinataires) {
            User agent = getAgentFromMatricule(matricule);
            if (agent != null) {
                emailInfo.addTo(agent.getMail(), agent.getNom());
            }
        }

        LOGGER.info("Liste des adresses destinataires de cet e-mail : {}", emailInfo.getTo());

        emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, execution.getProcessInstanceBusinessKey());
        emailInfo.setLangue("fr");

        DemandeDTO demande = demandesService.getDemande(demandeId);

        Map<String, Object> model = afMailTemplateModelProvider.getModel(subjectTemplateCode, bodyTemplateCode, demande,
                execution.getVariables(), null, null);

        try {
            mailService.sendMail(emailInfo, model, MailAudienceEnum.AGENT);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }

        LOGGER.info("==== xaf-denjs ENVOI EMAIL AGENT DE L'ETABLISSEMENT AFFECTÉ <fin>");
    }

    private User getAgentFromMatricule(String matricule) {
        User agent = utilisateursCache.get(matricule);
        if (agent == null) {
            LOGGER.warn("Attention, l'agent de matricule {} n'a pas pu être trouvé ! ", matricule);
            return null;
        }
        if (StringUtils.isBlank(agent.getMail())) {
            LOGGER.warn("L'agent ({},{}) n'a pas d'e-mail renseigné !", matricule, agent.getNom());
            return null;
        }
        // Vérifier que l'agent a bien encore des droits sur cette appli
        if (StringUtils.isBlank(agent.getRolesByAppli(gouvPropertiesResolver.getDemarcheId()))) {
            LOGGER.warn("L'agent ({},{}) n'a pas de droits sur cette appli !", matricule, agent.getNom());
            return null;
        }
        return agent;
    }

}

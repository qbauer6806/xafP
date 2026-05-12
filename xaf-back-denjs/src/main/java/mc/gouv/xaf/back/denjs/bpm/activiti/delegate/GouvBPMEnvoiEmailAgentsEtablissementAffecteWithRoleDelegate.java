package mc.gouv.xaf.back.denjs.bpm.activiti.delegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import mc.gouv.xaf.back.denjs.dto.DenjsAffectationAgentDTO;
import mc.gouv.xaf.back.denjs.service.DenjsAffectationService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.dto.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.impl.AfMailTemplateModelProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemarcheDTO;
import mc.gouv.xaf.shared.enums.MailAudienceEnum;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Classe service appelée par le process Activiti pour envoyer un email aux agents affectés à l'établissement à laquelle
 * est affectée la demande selon le droit defini dans le bpm
 *
 * @author qdeme
 */
@Component
@RequiredArgsConstructor
public class GouvBPMEnvoiEmailAgentsEtablissementAffecteWithRoleDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            GouvBPMEnvoiEmailAgentsEtablissementAffecteWithRoleDelegate.class);

    private final AfBackUtils afBackUtils;
    private final DemarchesService demarchesService;
    private final MailService mailService;
    private final DemandesService demandesService;
    private final GouvPropertiesResolver gouvPropertiesResolver;
    private final AfMailTemplateModelProvider afMailTemplateModelProvider;
    private final UtilisateursCache utilisateursCache;
    private final DenjsAffectationService denjsAffectationService;

    @Setter
    @Getter
    private Expression emailBodyTemplateCode;

    @Setter
    @Getter
    private Expression emailSubjectTemplateCode;

    @Setter
    @Getter
    private Expression emailTemplateCode;

    @Setter
    @Getter
    private Expression roles;

    @Override
    public void execute(DelegateExecution execution) {

        LOGGER.info("==== xaf-denjs ENVOI EMAIL AGENT DE L'ETABLISSEMENT AFFECTÉ ...");

        String bodyTemplateCode = mailService.getEmailBodyTemplate(emailBodyTemplateCode, emailTemplateCode, execution);
        String subjectTemplateCode = mailService.getEmailSubjectTemplate(emailSubjectTemplateCode, emailTemplateCode,
                execution);

        LOGGER.info("bodyTemplateCode : {}", bodyTemplateCode);
        LOGGER.info("subjectTemplateCode : {}", subjectTemplateCode);

        Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());

        String rolesStr = (String) roles.getValue(execution);
        String[] rolesList = rolesStr.split(",");
        Set<String> listAgentWithRole = afBackUtils.getAgentsWithRoles(rolesList).stream().map(User::getMatricule)
                .collect(Collectors.toSet());

        List<String> matriculesDestinataires = new ArrayList<>();
        String etablissementCode = denjsAffectationService.getAffectationDemandeEtablissement(demandeId);
        if (etablissementCode != null) {
            List<DenjsAffectationAgentDTO> affectations = denjsAffectationService.getAffectationsAgents();
            for (DenjsAffectationAgentDTO affectation : affectations) {
                if (listAgentWithRole.contains(affectation.getAgentMatricule()) && affectation.getEtablissementCode()
                        .equals(etablissementCode)) {
                    matriculesDestinataires.add(affectation.getAgentMatricule());
                }
            }
        }

        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        DemarcheDTO demarcheDTO = demarchesService.getDemarche();
        emailInfo.setFrom(demarcheDTO.getEmailFrom(), demarcheDTO.getEmailFromNom());
        emailInfo.setReplyto(demarcheDTO.getEmailReplyto(), demarcheDTO.getEmailReplytoNom());

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

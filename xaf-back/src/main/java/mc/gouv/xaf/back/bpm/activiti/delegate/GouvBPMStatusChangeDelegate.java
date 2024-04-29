package mc.gouv.xaf.back.bpm.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.StatutInputDTO;

/**
 * 
 * Classe service appelée par le process Activiti pour changer le statut d'une demande.
 * 
 * @author qdeme
 *
 */
@Component
public class GouvBPMStatusChangeDelegate implements JavaDelegate {

    // voir pour l'autowiring dans les javaDelegate

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMStatusChangeDelegate.class);

    private Expression targetState;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemandesStatutsService demandesStatutsService;

    private Expression codeMotif;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        LOGGER.info("==== xaf-back CHANGEMENT STATUT ...");

        String statut = getTargetState(execution);

        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());

        LOGGER.info("Demande : {}", demandeId);
        LOGGER.info("Statut à mettre : {}", statut);

        String codeMotifStr = null;
        if (codeMotif != null && codeMotif.getValue(execution) != null) {
            codeMotifStr = (String) codeMotif.getValue(execution);
        }

        // Récupération du commentaire usager, du texte à envoyer et du code motif si besoin plus tard dans le traitement
        String commentaireUsager = (String) execution
                .getVariable(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());
        String texteAEnvoyer = (String) execution
                .getVariable(GouvBPMProcessVariableTypeEnum.MC_TEXTE_A_ENVOYER.name());

        // Si le code motif n'a pas été indiqué dans le BPMN, alors le récupérer des process variables
        if (StringUtils.isBlank(codeMotifStr)) {
            codeMotifStr = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());
        }

        String agentId = (String) execution
                .getVariable(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_AGENT.name());
        String usagerId = (String) execution
                .getVariable(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_USAGER.name());

        LOGGER.info("Commentaire usager : {}", commentaireUsager);
        LOGGER.info("Texte à envoyer : {}", texteAEnvoyer);
        LOGGER.info("Code motif : {}", codeMotifStr);

        LOGGER.info("Appel à DEM changerStatutDemande()...");

        // Définition de la personne à l'origine du changement de statut : soit on l'a indiqué à ce JavaDelegate
        // via des variables process (que ce soit un agent ou un usager), soit on n'a rien indiqué et on prend
        // par défaut l'agent authentifié
        StatutInputDTO statutInput = new StatutInputDTO();
        if (usagerId != null) {
            demandesStatutsService.updateStatut(gouvPropertiesResolver.getDemarcheId(), demandeId, statut, null,
                    Integer.parseInt(usagerId), codeMotifStr, commentaireUsager, texteAEnvoyer);
            statutInput.setUsagerId(Integer.parseInt(usagerId));
        } else if (agentId != null) {
            demandesStatutsService.updateStatut(gouvPropertiesResolver.getDemarcheId(), demandeId, statut, agentId,
                    null, codeMotifStr, commentaireUsager, texteAEnvoyer);
        } else {
            demandesStatutsService.updateStatut(gouvPropertiesResolver.getDemarcheId(), demandeId, statut,
                    AfBackUtils.getAuthenticatedAgentId(), null, codeMotifStr, commentaireUsager, texteAEnvoyer);
        }

        LOGGER.info("==== xaf-back CHANGEMENT STATUT <fin>");
    }

    public Expression getTargetState() {
        return targetState;
    }

    public void setTargetState(Expression targetState) {
        this.targetState = targetState;
    }

    /**
     * Permet de retourner l'état dans lequel il faut mettre la demande Soit il a été défini dans le .bpmn (
     * <serviceTask><extensionElements><activiti:field name="targetState"><activiti:string>AFFECTEE ...) Soit il a été
     * défini dans la variable "targetState" du process auparavant
     */
    private String getTargetState(DelegateExecution execution) {
        if (targetState != null) {
            // L'état cible a été indiqué dans le .bpmn
            return (String) targetState.getValue(execution);
        } else {
            // L'état cible a été indiqué dans une variable du process
            return (String) execution.getVariableLocal(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE.name());
        }
    }

}

package mc.gouv.xaf.back.bpm.activiti.delegate;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Classe service appelée par le process Activiti pour changer le statut d'une demande.
 *
 * @author qdeme
 */
@Component
@Scope("prototype") // Indispensable pour éviter que les champs persistent entre exécutions du délegate
@RequiredArgsConstructor
public class GouvBPMStatusChangeDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMStatusChangeDelegate.class);

    @Setter
    private Expression targetState;
    @Setter
    private Expression codeMotif;

    private final DemandesStatutsService demandesStatutsService;

    @Override
    public void execute(DelegateExecution execution) {

        LOGGER.info("==== xaf-back CHANGEMENT STATUT ...");

        String statutName = getTargetState(execution);

        Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());

        LOGGER.info("Demande : {}", demandeId);
        LOGGER.info("Statut à mettre : {}", statutName);

        String codeMotifStr = null;
        if (codeMotif != null && codeMotif.getValue(execution) != null) {
            codeMotifStr = (String) codeMotif.getValue(execution);
        }

        // Récupération du commentaire usager, du texte à envoyer et du code motif si besoin plus tard dans le traitement
        String commentaireUsager = (String) execution.getVariable(
                GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());
        String texteAEnvoyer = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_TEXTE_A_ENVOYER.name());

        // Si le code motif n'a pas été indiqué dans le BPMN, alors le récupérer des process variables
        if (StringUtils.isBlank(codeMotifStr)) {
            codeMotifStr = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());
        }

        String agentId = (String) execution.getVariable(
                GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_AGENT.name());
        String usagerId = (String) execution.getVariable(
                GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_USAGER.name());

        LOGGER.info("Commentaire usager : {}", commentaireUsager);
        LOGGER.info("Texte à envoyer : {}", texteAEnvoyer);
        LOGGER.info("Code motif : {}", codeMotifStr);

        LOGGER.info("Appel à DEM changerStatutDemande()...");

        // Définition de la personne à l'origine du changement de statut : soit on l'a indiqué à ce JavaDelegate
        // via des variables process (que ce soit un agent ou un usager), soit on n'a rien indiqué et on prend
        // par défaut l'agent authentifié
        if (usagerId != null) {
            demandesStatutsService.updateStatut(demandeId, statutName, null, Integer.parseInt(usagerId), codeMotifStr,
                    commentaireUsager, texteAEnvoyer);
        } else if (agentId != null) {
            demandesStatutsService.updateStatut(demandeId, statutName, agentId, null, codeMotifStr, commentaireUsager,
                    texteAEnvoyer);
        } else {
            demandesStatutsService.updateStatut(demandeId, statutName, AfBackUtils.getAuthenticatedAgentId(), null,
                    codeMotifStr, commentaireUsager, texteAEnvoyer);
        }

        LOGGER.info("==== xaf-back CHANGEMENT STATUT <fin>");
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

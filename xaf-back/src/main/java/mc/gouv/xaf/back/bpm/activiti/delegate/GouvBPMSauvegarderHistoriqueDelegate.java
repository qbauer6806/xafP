package mc.gouv.xaf.back.bpm.activiti.delegate;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.histo.DemandesHistoriqueService;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Classe appelée par activiti afin d'ajouter l'hisorique de changement de statut
 *
 * @author mboutelier.ext
 */
@Component
@Scope("prototype") // Indispensable pour éviter que les champs persistent entre exécutions du délegate
@RequiredArgsConstructor
public class GouvBPMSauvegarderHistoriqueDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMSauvegarderHistoriqueDelegate.class);

    @Setter
    private Expression targetState;
    @Setter
    private Expression sourceState;
    @Setter
    private Expression executionRole;

    private final DemandesHistoriqueService demandesHistoriqueService;
    private final DemarchesDataProvider demarchesDataProvider;

    @Override
    public void execute(DelegateExecution execution) {

        LOGGER.info("==== xaf-back SAUVEGARDE HISTORIQUE ...");

        String statut = getTargetState(execution);
        Integer pkDemande = Integer.parseInt(execution.getProcessInstanceBusinessKey());

        LOGGER.info("targetState = {}, pkDemande = {} ...", statut, pkDemande);

        // Ajout d'une ligne à l'historique
        String role = executionRole != null ? (String) executionRole.getValue(execution) : null;
        String dernierStatut = sourceState != null ? (String) sourceState.getValue(execution) : null;
        String action = demarchesDataProvider.getHistoAction(statut, null, dernierStatut, null);
        demandesHistoriqueService.ajouterHistorique(pkDemande, statut, role, action, null);

        LOGGER.info("==== xaf-back SAUVEGARDE HISTORIQUE <FIN>");
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

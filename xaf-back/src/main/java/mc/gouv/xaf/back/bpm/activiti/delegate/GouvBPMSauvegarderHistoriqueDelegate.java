package mc.gouv.xaf.back.bpm.activiti.delegate;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.service.histo.DemandesHistoriqueService;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Classe appelée par activiti afin d'ajouter l'hisorique de changement de statut
 *
 * @author mboutelier.ext
 */
@Component
public class GouvBPMSauvegarderHistoriqueDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMSauvegarderHistoriqueDelegate.class);

    @Setter
    @Getter
    private Expression targetState;

    @Autowired
    private DemandesHistoriqueService demandesHistoriqueService;

    @Override
    public void execute(DelegateExecution execution) {

        LOGGER.info("==== xaf-back SAUVEGARDE HISTORIQUE ...");

        String statut = getTargetState(execution);
        Integer pkDemande = Integer.parseInt(execution.getProcessInstanceBusinessKey());

        LOGGER.info("targetState = {}, pkDemande = {} ...", statut, pkDemande);

        // Ajout d'une ligne à l'historique
        DemandeHistoriqueDTO histo = demandesHistoriqueService.statusChangeAgent(statut);
        demandesHistoriqueService.saveHisto(pkDemande, histo);
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

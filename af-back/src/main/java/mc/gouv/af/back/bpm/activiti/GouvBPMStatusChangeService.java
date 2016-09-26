package mc.gouv.af.back.bpm.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.Static;
import mc.gouv.af.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.dem.apiclient.DemClient;
import mc.gouv.dem.apishared.model.DemandeStatutEnum;
import mc.gouv.dem.apishared.model.StatutInputDTO;

/**
 * Classe service appelée par le process Activiti pour changer le statut d'une demande.
 * 
 * @author qdeme
 *
 */
public class GouvBPMStatusChangeService implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMStatusChangeService.class);
    
    private Expression targetState;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("==== AF-BACK CHANGEMENT STATUT ...");
        
        DemandeStatutEnum statut = getTargetState(execution);
        
//        // Appel à DEM
//        DemClient demClient = new DemClient(Static.getValue("mc.gouv.dem.url"), Static.getValue("mc.gouv.dem.user"), Static.getValue("mc.gouv.dem.pwd"));
//        StatutInputDTO statutInput = new StatutInputDTO();
//        statutInput.set
//        demClient.changerStatutDemande(Static.getValue("mc.gouv.af.back.demarcheId"), execution.getBusinessKey(), statut);
        
        LOGGER.info("Statut à mettre : " + statut);
    }

    public Expression getTargetState() {
        return targetState;
    }

    public void setTargetState(Expression targetState) {
        this.targetState = targetState;
    }

    /**
     * Permet de retourner l'état dans lequel il faut mettre la demande
     * Soit il a été défini dans le .bpmn (<serviceTask><extensionElements><activiti:field name="targetState"><activiti:string>AFFECTEE ...)
     * Soit il a été défini dans la variable "targetState" du process auparavant
     * @param execution
     * @return
     */
    private DemandeStatutEnum getTargetState(DelegateExecution execution) {
        if (targetState != null) {
            // L'état cible a été indiqué dans le .bpmn
            return DemandeStatutEnum.valueOf((String)targetState.getValue(execution));
        }
        else {
            // L'état cible a été indiqué dans une variable du process
            return DemandeStatutEnum.valueOf((String)execution.getVariableLocal(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE.name()));
        }
    }
    
}

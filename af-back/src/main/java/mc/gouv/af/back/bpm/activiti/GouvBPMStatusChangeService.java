package mc.gouv.af.back.bpm.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.af.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.dem.apiclient.DemClient;
import mc.gouv.dem.apishared.model.DemandeComplementsQuestionDTO;
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
        
        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());
        
        LOGGER.info("Demande : " + demandeId);
        LOGGER.info("Statut à mettre : " + statut);
        
        DemClient demClient = new DemClient(AfBackUtils.getDemUrl(), AfBackUtils.getDemUser(), AfBackUtils.getDemPwd());
        
        // Récupération du commentaire usager et du code motif si besoin plus tars dans le traitement
        String commentaireUsager = (String)execution.getVariables().get(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());
        String codeMotif = (String)execution.getVariables().get(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());
        
        if (!statut.equals(DemandeStatutEnum.EN_ATTENTE_COMPL)) {
            // Statut différent de EN_ATTENTE_COMPL (on peut le traiter avec un appel à DEM pour changer le statut)
            StatutInputDTO statutInput = new StatutInputDTO();
            statutInput.setAgentId(AfBackUtils.getAuthenticatedAgentId());
            statutInput.setStatut(statut);
            if (statut.equals(DemandeStatutEnum.ACCEPTEE) || statut.equals(DemandeStatutEnum.REFUSEE)) {
                // Si statut ACCEPTEE ou REFUSEE, indiquer un commentaire usager et un motif
                LOGGER.info("Statut requérant l'indication d'un commentaire usager et d'un code motif");
                LOGGER.info("Commentaire usager : " + commentaireUsager);
                LOGGER.info("Code motif : " + codeMotif);
                statutInput.setCommentaire(commentaireUsager);
                statutInput.setCodeMotif(codeMotif);
            }
            
            LOGGER.info("Appel à DEM changerStatutDemande() (" + AfBackUtils.getDemUrl() + ")...");
            demClient.changerStatutDemande(AfBackUtils.getDemarcheId(), demandeId, statutInput);
        }
        else {
            // Statut EN_ATTENTE_COMPL, il convient alors de créer dans DEM une demande d'informations complémentaires
            DemandeComplementsQuestionDTO questionDto = new DemandeComplementsQuestionDTO();
            questionDto.setAgentId(AfBackUtils.getAuthenticatedAgentId());
            questionDto.setCodeMotif(codeMotif);
            questionDto.setTexte(commentaireUsager);
            
            LOGGER.info("Appel à DEM createDemandeComplements() (" + AfBackUtils.getDemUrl() + ")...");
            demClient.createDemandeComplements(AfBackUtils.getDemarcheId(), demandeId, questionDto);
        }
        
        LOGGER.info("==== AF-BACK CHANGEMENT STATUT <fin>");
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

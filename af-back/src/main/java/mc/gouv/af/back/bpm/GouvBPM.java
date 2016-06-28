package mc.gouv.af.back.bpm;

import java.util.List;
import java.util.Map;

import mc.gouv.af.back.bpm.model.GouvBPMGroup;
import mc.gouv.af.back.bpm.model.GouvBPMTask;
import mc.gouv.af.back.bpm.model.GouvBPMUser;

/**
 * Composant exposant le BPM interne d'AppFactory
 * 
 * @author qdeme
 *
 */
public interface GouvBPM {

    /**
     * Permet de démarrer une instance de process
     * @param processDefinitionKey Identifiant du process
     * @param user Utilisateur à l'origine de l'action
     * @param demandeId Identifiant de la demande liée à l'instance
     * @param codeAppli Code de l'application concernée
     * @param businessVariables Variables métier destinées à être stockées dans l'instance
     */
    public void startProcessInstance(String processDefinitionKey, GouvBPMUser user, String demandeId, String codeAppli, Map<String, Object> businessVariables);
    
    /**
     * Permet de récupérer les variables métier liées à une instance de process
     * @param demandeId Identifiant de la demande correspondant à l'instance de process
     * @return Les variables métier demandées
     */
    public Map<String, Object> getProcessBusinessVariables(String demandeId);
    
    /**
     * Permet de définir les variables métier liées à une instance de process
     * @param businessVariables
     */
    public void setProcessBusinessVariables(String demandeId, Map<String, Object> businessVariables);
    
    /**
     * Permet à un utilisateur de s'affecter une tâche
     * @param task Tâche concernée
     * @param user Utilisateur à assigner
     */
    public void claimTask(GouvBPMTask task, GouvBPMUser user);
    
    /**
     * Permet de finaliser une tâche
     * @param taskId Identifiant de la tâche concernée
     */
    public void completeTask(GouvBPMTask task);
    
    /**
     * Permet de lister les tâches actives sur lesquelles un utilisateur est assigné
     * @param user Utilisateur
     * @return
     */
    public List<GouvBPMTask> getTasksAssignedToUser(GouvBPMUser user);
    
    /**
     * Permet de lister les tâches actives concernant une demande
     * @param demandeId Identifiant de la demande
     * @return
     */
    public List<GouvBPMTask> getActiveTasksForDemande(String demandeId);

    /**
     * Permet de lister les tâches pour lesquelles un utilisateur est désigné comme candidat
     * @param user Utilisateur concerné
     * @param codeAppli Code de l'application concernée
     * @return
     */
    public List<GouvBPMTask> getTasksWhereUserIsCandidate(GouvBPMUser user, String codeAppli);
    
    /**
     * Permet de lister les tâches pour lesquelles un groupe est désigné comme candidat
     * @param group Groupe concerné
     * @param codeAppli Code de l'application concernée
     * @return
     */
    public List<GouvBPMTask> getTasksWhereGroupIsCandidate(GouvBPMGroup group, String codeAppli);
    
    /**
     * Permet de savoir si l'instance de process liée à une demande est vivante ou terminée
     * @param demandeId
     * @return
     */
    public boolean isProcessInstanceAlive(String demandeId);
    
    /**
     * Permet de sauter d'une tâche à une autre
     * @param demandeId
     * @param taskFrom
     * @param taskTo
     */
    public void jump(String demandeId, GouvBPMTask taskFrom, GouvBPMTask taskTo);
    
}

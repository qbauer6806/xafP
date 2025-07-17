package mc.gouv.xaf.back.bpm;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.back.bpm.activiti.exception.TaskAlreadyClaimedException;
import mc.gouv.xaf.back.bpm.model.GouvBPMStatutAction;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

/**
 * Composant exposant le BPM interne d'AppFactory
 *
 * @author qdeme
 */
public interface GouvBPM {

    /**
     * Permet de démarrer une instance de process
     *
     * @param processDefinitionKey
     *         Identifiant du process
     * @param user
     *         Utilisateur à l'origine de l'action
     * @param demandeId
     *         Identifiant de la demande liée à l'instance
     * @param businessVariables
     *         Variables métier destinées à être stockées dans l'instance
     */
    void startProcessInstance(String processDefinitionKey, GouvBPMUser user, Integer demandeId,
            Map<String, Object> businessVariables);

    /**
     * Permet de récupérer les variables métier liées à une instance de process
     *
     * @param demandeId
     *         Identifiant de la demande correspondant à l'instance de process
     * @return Les variables métier demandées
     */
    Map<String, Object> getProcessBusinessVariables(Integer demandeId);

    /**
     * Permet de définir les variables métier liées à une instance de process
     *
     * @param demandeId
     *         Identifiant de la demande correspondant à l'instance de process
     * @param businessVariables
     *         les variables à lier
     */
    void setProcessBusinessVariables(Integer demandeId, Map<String, Object> businessVariables);

    /**
     * Permet de définir une variable métier liée à une instance de process
     *
     * @param demandeId
     *         Identifiant de la demande correspondant à l'instance de process
     * @param key
     *         clé de la varible
     * @param value
     *         valeur de la variable
     */
    void setProcessBusinessVariable(Integer demandeId, String key, Object value);

    /**
     * Permet à un utilisateur de s'affecter une tâche
     *
     * @param task
     *         Tâche concernée
     * @param user
     *         Utilisateur à assigner
     * @throws TaskAlreadyClaimedException
     *         Exception indiquant que la tâche est déjà affectée
     */
    void claimTask(GouvBPMTask task, GouvBPMUser user) throws TaskAlreadyClaimedException;

    /**
     * Permet de finaliser une tâche
     *
     * @param task
     *         Tâche concernée
     * @param demandeId
     *         Identifiant de la demande correspondant à l'instance de process
     */
    void completeTask(GouvBPMTask task, Integer demandeId) throws IOException, TikaException, SAXException;

    /**
     * Permet de lister les tâches actives concernant une demande
     *
     * @param demandeId
     *         Identifiant de la demande
     * @return Une liste de tâches appartenant à la demande
     */
    List<GouvBPMTask> getActiveTasksForDemande(Integer demandeId);

    /**
     * Permet de savoir si l'instance de process liée à une demande est vivante ou terminée
     *
     * @param demandeId
     *         l'id de la demande
     * @return L'état de l'instance
     */
    boolean isProcessInstanceAlive(Integer demandeId);

    /**
     * Permet de compléter une tâche en lui donnant les données du formulaire qu'elle requiert
     *
     * @param task
     *         La tâche à compléter
     * @param properties
     *         Les propriétés du formulaire
     * @param demandeId
     *         L'id de la demande
     * @throws IOException,
     *         TikaException, SAXException
     */
    void submitTaskFormData(GouvBPMTask task, Map<String, String> properties, Integer demandeId)
            throws IOException, TikaException, SAXException;

    /**
     * Permet d'obtenir la liste disponible d'associations action / statut cible, depuis le BPM, pour une tâche
     *
     * @param task
     *         La tâche à évaluer
     * @return La liste des actions pour la tâche donnée
     */
    List<GouvBPMStatutAction> getTaskStatutActions(GouvBPMTask task);

    /**
     * Permet de supprimer une variable du process
     *
     * @param demandeId
     *         L'id de la demande
     * @param businessVariable
     *         La variable à supprimer
     */
    void removeProcessBusinessVariables(Integer demandeId, String businessVariable);

    /**
     * Annulation d'une demande, par un agent ou un usager (mettre l'un ou l'autre)
     *
     * @param demandeId
     *         L'id de la demande à annuler
     * @param agent
     *         L'agent appelant l'action
     * @param usager
     *         L'usager appelant l'action
     * @param commentaire
     *         Commentaire optionnel
     * @param codeMotif
     *         Le motif de l'annulation
     * @param statutAnnulation
     *         Le statut de l'annulation
     */
    void annulerDemande(Integer demandeId, GouvBPMUser agent, GouvBPMUser usager, String codeMotif, String commentaire,
            String statutAnnulation);

    /**
     * Permet de démarrer une instance de process sur message
     *
     * @param messageName
     *         Message correspondant
     * @param user
     *         Utilisateur à l'origine de l'action
     * @param demandeId
     *         Identifiant de la demande liée à l'instance
     * @param businessVariables
     *         Variables métier destinées à être stockées dans l'instance
     */
    void startProcessInstanceByMessage(String messageName, GouvBPMUser user, Integer demandeId,
            Map<String, Object> businessVariables);

    void setAssignee(Integer demandeId, String assignee);

    void reponseRectification(Integer pkDemande, Integer usagerId);

    void rectificationSpontanee(Integer demandeId);

}

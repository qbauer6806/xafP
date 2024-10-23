package mc.gouv.xaf.back.bpm;

import mc.gouv.xaf.back.bpm.activiti.exception.TaskAlreadyClaimedException;
import mc.gouv.xaf.back.bpm.model.*;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
     * @param codeAppli
     *         Code de l'application concernée
     * @param businessVariables
     *         Variables métier destinées à être stockées dans l'instance
     */
    void startProcessInstance(String processDefinitionKey, GouvBPMUser user, Integer demandeId, String codeAppli,
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
     * Permet de lister les tâches actives sur lesquelles un utilisateur est assigné
     *
     * @param user
     *         Utilisateur
     * @return Une liste de tâche assignées à l'utilisateur
     */
    @SuppressWarnings("unused")
    List<GouvBPMTask> getTasksAssignedToUser(GouvBPMUser user);

    /**
     * Permet de lister les tâches actives concernant une demande
     *
     * @param demandeId
     *         Identifiant de la demande
     * @return Une liste de tâches appartenant à la demande
     */
    List<GouvBPMTask> getActiveTasksForDemande(Integer demandeId);

    /**
     * Permet de récupérer le nombres de tâches dans un certain état
     *
     * @param name
     *         Le nom de l'état
     * @return La liste des ids dont les tâches sont à l'état donné
     */
    List<String> getNumberActiveDemandesInState(String name);

    /**
     * Permet de lister les tâches pour lesquelles un utilisateur est désigné comme candidat
     *
     * @param user
     *         Utilisateur concerné
     * @param codeAppli
     *         Code de l'application concernée
     * @return La liste des tâches
     */
    @SuppressWarnings("unused")
    List<GouvBPMTask> getTasksWhereUserIsCandidate(GouvBPMUser user, String codeAppli);

    /**
     * Permet de lister les tâches d'une demande pour lesquelles un utilisateur est désigné comme candidat
     *
     * @param user
     *         Utilisateur concerné
     * @param codeAppli
     *         Code de l'application concernée
     * @param demandeId
     *         Demande concernée
     * @return La liste des tâches associées à l'utilisateur
     */
    @SuppressWarnings("unused")
    List<GouvBPMTask> getTasksForDemandeWhereUserIsCandidate(GouvBPMUser user, String codeAppli, Integer demandeId);

    /**
     * Permet de lister les tâches pour lesquelles un groupe est désigné comme candidat
     *
     * @param group
     *         Groupe concerné
     * @param codeAppli
     *         Code de l'application concernée
     * @return La liste des tâches associées à l'utilisateur
     */
    @SuppressWarnings("unused")
    List<GouvBPMTask> getTasksWhereGroupIsCandidate(GouvBPMGroup group, String codeAppli);

    /**
     * Permet de savoir si l'instance de process liée à une demande est vivante ou terminée
     *
     * @param demandeId
     *         l'id de la demande
     * @return L'état de l'instance
     */
    boolean isProcessInstanceAlive(Integer demandeId);

    /**
     * Permet de sauter d'une tâche à une autre
     *
     * @param demandeId
     *         L'id de la demande associée aux tâches
     * @param taskFrom
     *         La tâche actuelle
     * @param taskTo
     *         La tâche suivante
     */
    @SuppressWarnings("unused")
    void jump(Integer demandeId, GouvBPMTask taskFrom, GouvBPMTask taskTo);

    /**
     * Permet de lister les DemandeID d'une demarche (codeAppli) qui sont dans une certaine tâche courante Exemple : on
     * souhaite lister toutes les demandes en attente de validation d'une démarche
     *
     * @param codeAppli
     *         Le code appli de la démarche
     * @param task
     *         La tâche à filtrer
     * @return L'id des demandes de la démarche à la tâche donnée
     */
    @SuppressWarnings("unused")
    List<Integer> getDemandesIdsByCodeAppliAndTacheCourante(String codeAppli, GouvBPMTask task);

    /**
     * Permet de lister les DemandeID d'une demarche (codeAppli) qui sont dans une certaine tâche courante et qu'un
     * certain utilisateur pourrait compléter. Exemple : on souhaite lister toutes les demandes en attente de validation
     * d'une démarche par un certain utilisateur
     *
     * @param codeAppli
     *         Le code appli de la démarche
     * @param task
     *         La tâche à filtrer
     * @param user
     *         L'utilisateur à filtrer
     * @return L'id des demandes de la démarche à la tâche donnée
     */
    @SuppressWarnings("unused")
    List<Integer> getDemandesIdsByCodeAppliAndTacheCouranteAndCandidateUser(String codeAppli, GouvBPMTask task,
            GouvBPMUser user);

    /**
     * Permet de récupérer les commentaires internes liés à une demande
     *
     * @param demandeId
     *         L'id de la demande
     * @return La liste des commentaires
     */
    List<CommentaireInterneDTO> getCommentairesInternes(Integer demandeId);

    /**
     * Permet d'ajouter un commentaire interne à une demande
     *
     * @param demandeId
     *         L'id de la demande
     * @param commentaire
     *         Le commentaire à ajouter
     */
    void putCommentaireInterne(Integer demandeId, CommentaireInterneDTO commentaire);

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
     * @param codeAppli
     *         Code de l'application concernée
     * @param businessVariables
     *         Variables métier destinées à être stockées dans l'instance
     */
    void startProcessInstanceByMessage(String messageName, GouvBPMUser user, Integer demandeId, String codeAppli,
            Map<String, Object> businessVariables);

    void setAssignee(Integer demandeId, String assignee);

    void demanderRectification(Integer demandeId, GouvBPMUser agent, String codeMotif, String commentaire,
            String statutAnnulation);

    void reponseRectification(Integer pkDemande, Integer usagerId)
            throws TaskAlreadyClaimedException, IOException, SAXException;

    void rectificationSpontanee(Integer demandeId);

}

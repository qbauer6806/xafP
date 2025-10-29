package mc.gouv.xaf.back.bpm.activiti;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.GouvBPMException;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.bpm.activiti.exception.TaskAlreadyClaimedException;
import mc.gouv.xaf.back.bpm.model.GouvBPMStatutAction;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.TikaException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.api.FlowableTaskAlreadyClaimedException;
import org.flowable.engine.FormService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.form.FormProperty;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Composant exposant le BPM interne d'AppFactory
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class GouvBPMImpl implements GouvBPM {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMImpl.class);
    private static final String NULL_PI = "ProcessInstance null !";
    public static final String ANNULATION_MESSAGE = "annulationMessage";

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FormService formService;

    private void startProcessInstanceByKeyOrMessage(String processDefinitionKey, String messageName, GouvBPMUser user,
            Integer demandeId, Map<String, Object> businessVariables) {
        LOGGER.info(
                "startProcessInstance() Démarrage d'une instance du process \"{}\" assignée à l'utilisateur \"{}\" et concernant la demande \"{}\"",
                processDefinitionKey, user, demandeId);

        // Création des variables du process
        Map<String, Object> variables = new HashMap<>();
        // Variables techniques
        variables.put(GouvBPMProcessVariableTypeEnum.MC_USERID.name(), user.getId());
        variables.put(GouvBPMProcessVariableTypeEnum.MC_EXPIRED.name(), false);
        // Variables business éventuellement fournies par le client
        if (businessVariables != null) {
            variables.putAll(businessVariables);
        }

        ProcessInstance process;
        try {
            // On utilise le demandeId pour la "businessKey"
            if (!StringUtils.isBlank(processDefinitionKey)) {
                process = runtimeService.startProcessInstanceByKey(processDefinitionKey, demandeId.toString(),
                        variables);
            } else {
                process = runtimeService.startProcessInstanceByMessage(messageName, demandeId.toString(), variables);
            }
            LOGGER.info("Instance démarrée : {}, {}, {}, {}, {}", process.getDeploymentId(), process.getActivityId(),
                    process.getId(), process.getDescription(), process.getProcessInstanceId());
        } catch (FlowableObjectNotFoundException e) {
            throw new GouvBPMException("Erreur lors du démarrage de l'instance de process", e);
        }

    }

    @Override
    public void startProcessInstance(String processDefinitionKey, GouvBPMUser user, Integer demandeId,
            Map<String, Object> businessVariables) {

        startProcessInstanceByKeyOrMessage(processDefinitionKey, null, user, demandeId, businessVariables);

    }

    @Override
    public void startProcessInstanceByMessage(String messageName, GouvBPMUser user, Integer demandeId,
            Map<String, Object> businessVariables) {

        startProcessInstanceByKeyOrMessage(null, messageName, user, demandeId, businessVariables);

    }

    @Override
    public Map<String, Object> getProcessBusinessVariables(Integer demandeId) {
        LOGGER.debug("getProcessBusinessVariables({})", demandeId);
        ProcessInstance processInstance = getActiveProcessInstanceForDemandeId(demandeId);
        if (processInstance == null) {
            LOGGER.error(NULL_PI);
            return new HashMap<>();
        }
        return runtimeService.getVariables(processInstance.getId());
    }

    @Override
    public void setProcessBusinessVariables(Integer demandeId, Map<String, Object> businessVariables) {
        LOGGER.debug("setProcessBusinessVariables({})", demandeId);
        ProcessInstance processInstance = getActiveProcessInstanceForDemandeId(demandeId);
        if (processInstance != null) {
            runtimeService.setVariables(processInstance.getId(), businessVariables);
        } else {
            LOGGER.error(NULL_PI);
        }
    }

    @Override
    public void setProcessBusinessVariable(Integer demandeId, String key, Object value) {
        LOGGER.debug("setProcessBusinessVariable({}, {}, {})", demandeId, key, value);
        ProcessInstance processInstance = getActiveProcessInstanceForDemandeId(demandeId);
        if (processInstance != null) {
            runtimeService.setVariable(processInstance.getId(), key, value);
        } else {
            LOGGER.error(NULL_PI);
        }
    }

    @Override
    public void removeProcessBusinessVariables(Integer demandeId, String businessVariable) {
        LOGGER.debug("removeProcessBusinessVariables({})", demandeId);
        ProcessInstance processInstance = getActiveProcessInstanceForDemandeId(demandeId);
        if (processInstance != null) {
            runtimeService.removeVariable(processInstance.getId(), businessVariable);
        } else {
            LOGGER.error(NULL_PI);
        }
    }

    @Override
    public List<GouvBPMTask> getActiveTasksForDemande(Integer demandeId) {
        LOGGER.debug("getActiveTasksForDemande({})", demandeId);

        List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(demandeId.toString()).active()
                .list();
        return GouvBPMTransformer.toGouvModelTasks(tasks);
    }

    @Override
    @Transactional(noRollbackFor = { FlowableTaskAlreadyClaimedException.class, TaskAlreadyClaimedException.class })
    public void claimTask(GouvBPMTask task, GouvBPMUser user) throws TaskAlreadyClaimedException {
        LOGGER.debug("claimTask({}, {})", task, user);

        if (null == task || null == user) {
            LOGGER.error("La tâche ou le user est null.");
            throw new InvalidParameterException();
        }

        if (task.getAssignee() != null && !task.getAssignee().equals(user.getId())) {
            throw new TaskAlreadyClaimedException("Tâche déjà claimed par un autre user");
        }

        try {
            taskService.claim(task.getId(), user.getId());
        } catch (FlowableTaskAlreadyClaimedException e) {
            throw new TaskAlreadyClaimedException("Erreur lors du claim de la tache " + task + " pour le user :" + user,
                    e);
        }
    }

    @Override
    public void completeTask(GouvBPMTask task, Integer demandeId) {
        LOGGER.info("completeTask({})", task);
        taskService.complete(task.getId());
    }

    private ProcessInstance getActiveProcessInstanceForDemandeId(Integer demandeId) {
        LOGGER.info("getActiveProcessInstanceForDemandeId({})", demandeId);
        return runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(demandeId.toString()).active()
                .singleResult();
    }

    @Override
    public boolean isProcessInstanceAlive(Integer demandeId) {
        ProcessInstance processInstance = getActiveProcessInstanceForDemandeId(demandeId);
        return processInstance != null && !processInstance.isEnded();
    }

    @Override
    public void setAssignee(Integer demandeId, String assignee) {
        LOGGER.debug("setAssignee({}, {})", demandeId, assignee);
        ProcessInstance processInstance = getActiveProcessInstanceForDemandeId(demandeId);
        if (processInstance != null) {
            runtimeService.setVariable(processInstance.getId(), GouvBPMProcessVariableTypeEnum.MC_ASSIGNEE.name(),
                    assignee);

            // Si des tâches sont affectées directement à un certain utilisateur, il faut les réaffecter à celui qui vient de prendre en charge la demande
            List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(demandeId.toString()).active()
                    .list();

            if (tasks != null && !tasks.isEmpty()) {
                for (Task t : tasks) {
                    if (StringUtils.isNotBlank(t.getAssignee())) {
                        LOGGER.info("Reprise en charge de la tache {} par {} ", t.getDescription(), assignee);
                        t.setAssignee(assignee);
                        taskService.saveTask(t);
                    }
                }
            }

        } else {
            LOGGER.error(NULL_PI);
        }
    }

    @Override
    public void submitTaskFormData(GouvBPMTask task, Map<String, String> properties, Integer demandeId)
            throws TikaException {
        // Pour éviter les NPE dans Activiti et éviter d'avoir à déclarer de nouveaux HashMaps
        // si on ne veut rien transmettre dans le formulaire
        Map<String, String> propertiesSafe = (properties == null) ? new HashMap<>() : properties;
        formService.submitTaskFormData(task.getId(), propertiesSafe);
    }

    @SuppressWarnings({ "unchecked", "java:S2864" })
    @Override
    public List<GouvBPMStatutAction> getTaskStatutActions(GouvBPMTask task) {

        List<GouvBPMStatutAction> statutActions = new ArrayList<>();
        List<FormProperty> formProps = formService.getTaskFormData(task.getId()).getFormProperties();

        // Isoler le FormProperty correspondant à MC_TARGETSTATE
        for (FormProperty formProp : formProps) {
            if (formProp.getId().equals(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE.name())) {
                Map<String, String> map = (Map<String, String>) formProp.getType().getInformation("values");
                // Lister les valeurs de l'enum et créer les objets faisant l'association action / statut cible
                for (String key : map.keySet()) {
                    statutActions.add(new GouvBPMStatutAction(key, map.get(key)));
                }
            }
        }

        return statutActions;
    }

    @Override
    public void annulerDemande(Integer demandeId, GouvBPMUser agent, GouvBPMUser usager, String codeMotif,
            String commentaire, String statutAnnulation) {
        LOGGER.info("Annulation de la demande {} par l'agent '{}' ou l'usager {}", demandeId, agent, usager);
        List<Execution> executions = runtimeService.createExecutionQuery()
                .processInstanceBusinessKey(demandeId.toString(), true)
                .messageEventSubscriptionName(ANNULATION_MESSAGE).list();

        Map<String, Object> variables = new HashMap<>();
        variables.put(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name(), codeMotif);
        variables.put(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name(), commentaire);
        variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE.name(), statutAnnulation);
        if (usager != null) {
            variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_USAGER.name(), usager.getId());
        } else if (agent != null) {
            variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_AGENT.name(), agent.getId());
        }

        if (executions.isEmpty()) {
            throw new GouvBPMException("Aucune execution pour annuler la demande : " + demandeId);
        }

        for (Execution ex : executions) {
            runtimeService.messageEventReceived(ANNULATION_MESSAGE, ex.getId(), variables);
        }
    }

    @Override
    public void rectificationSpontanee(Integer demandeId) {
        LOGGER.info("Rectification spontanée de la demande {} par l'usager", demandeId);
        List<Execution> executions = runtimeService.createExecutionQuery()
                .processInstanceBusinessKey(demandeId.toString(), true)
                .messageEventSubscriptionName("rectificationMessage").list();

        Map<String, Object> variables = new HashMap<>();
        variables.put(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_RECTIFICATION_EN_COURS.name(), false);

        // Normalement il y en a une seule

        LOGGER.info("Nombre d'executions candidates à rectification spontanée pour la demande {} : {}", demandeId,
                executions.size());

        if (executions.isEmpty()) {
            throw new GouvBPMException(
                    "Aucune execution pour effectuer une rectification spontanée de la demande : " + demandeId);
        }

        for (Execution ex : executions) {
            runtimeService.messageEventReceived("rectificationMessage", ex.getId(), variables);
        }
    }

    @Override
    public void reponseRectification(Integer pkDemande, Integer usagerId) {
        LOGGER.info("Réponse à la demande de rectification de la demande {} par l'usager", pkDemande);

        GouvBPMUser user = new GouvBPMUser();
        user.setId(usagerId.toString());

        GouvBPMTask task = getActiveTasksForDemande(pkDemande).getFirst();

        try {
            claimTask(task, user);
        } catch (TaskAlreadyClaimedException e1) {
            throw new DemarcheException("Erreur lors du claim de la tache", e1);
        }
        completeTask(task, pkDemande);

    }

}

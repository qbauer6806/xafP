package mc.gouv.xaf.back.bpm.activiti;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.GouvBPMException;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.bpm.activiti.exception.TaskAlreadyClaimedException;
import mc.gouv.xaf.back.bpm.model.*;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 
 * Composant exposant le BPM interne d'AppFactory
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class GouvBPMImpl implements GouvBPM {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMImpl.class);
    private static final String NULL_PI = "ProcessInstance null !";

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private FormService formService;
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @Autowired(required = false)
    private IndexedDemandeService indexedDemandeService;

    public void startProcessInstanceByKeyOrMessage(String processDefinitionKey, String messageName, GouvBPMUser user,
            Integer demandeId, String codeAppli, Map<String, Object> businessVariables) {
        LOGGER.info("startProcessInstance() Démarrage d'une instance du process \"{}\" assignée à l'utilisateur \"{}\" et concernant la demande \"{}\"",
                processDefinitionKey, user, demandeId);

        // Création des variables du process
        Map<String, Object> variables = new HashMap<>();
        // Variables techniques
        variables.put(GouvBPMProcessVariableTypeEnum.MC_USERID.name(), user.getId());
        variables.put(GouvBPMProcessVariableTypeEnum.MC_CODEAPPLI.name(), codeAppli);
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
        } catch (ActivitiObjectNotFoundException e) {
            throw new GouvBPMException("Erreur lors du démarrage de l'instance de process", e);
        }

    }

    @Override
    public void startProcessInstance(String processDefinitionKey, GouvBPMUser user, Integer demandeId, String codeAppli,
            Map<String, Object> businessVariables) {

        startProcessInstanceByKeyOrMessage(processDefinitionKey, null, user, demandeId, codeAppli, businessVariables);

    }

    @Override
    public void startProcessInstanceByMessage(String messageName, GouvBPMUser user, Integer demandeId, String codeAppli,
            Map<String, Object> businessVariables) {

        startProcessInstanceByKeyOrMessage(null, messageName, user, demandeId, codeAppli, businessVariables);

    }

    @Override
    public Map<String, Object> getProcessBusinessVariables(Integer demandeId) {
        LOGGER.debug("getProcessBusinessVariables({})", demandeId);
        ProcessInstance processInstance = getActiveProcessInstanceForDemandeId(demandeId);
        if (processInstance == null) {
            LOGGER.error(NULL_PI);
            return null;
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
    public List<GouvBPMTask> getTasksAssignedToUser(GouvBPMUser user) {
        LOGGER.debug("getTasksAssignedToUser({})", user);
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(user.getId()).active().list();
        return GouvBPMTransformer.toGouvModelTasks(tasks);
    }

    @Override
    public List<GouvBPMTask> getActiveTasksForDemande(Integer demandeId) {
        LOGGER.debug("getActiveTasksForDemande({})", demandeId);

        List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(demandeId.toString()).active()
                .list();
        return GouvBPMTransformer.toGouvModelTasks(tasks);
    }

    @Override
    public List<String> getNumberActiveDemandesInState(String state) {
        LOGGER.debug("getNumberActiveDemandesInState({})", state);
        List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey(state).active().list();
        Set<String> tasksProcessIds = tasks.stream().map(Task::getProcessInstanceId).collect(Collectors.toSet());
        List<String> instancesIds = new ArrayList<>();
        if (!tasksProcessIds.isEmpty()) {
            List<ProcessInstance> processInstanceInTheState = runtimeService.createProcessInstanceQuery().processInstanceIds(tasksProcessIds).active().list();
            instancesIds = processInstanceInTheState.stream().map(ProcessInstance::getBusinessKey).collect(Collectors.toList());
        }
        return instancesIds;
    }

    @Override
    @Transactional(noRollbackFor = { ActivitiTaskAlreadyClaimedException.class, TaskAlreadyClaimedException.class })
    public void claimTask(GouvBPMTask task, GouvBPMUser user) throws TaskAlreadyClaimedException {
        LOGGER.info("claimTask({}, {})", task, user);

        if (null == task || null == user) {
            LOGGER.error("La tâche ou le user est null.");
            throw new InvalidParameterException();
        }

        if (task.getAssignee() != null && !task.getAssignee().equals(user.getId())) {
        	throw new TaskAlreadyClaimedException("Tâche déjà claimed par un autre user");
        }

        try {
            taskService.claim(task.getId(), user.getId());
        } catch (ActivitiTaskAlreadyClaimedException e) {
            throw new TaskAlreadyClaimedException("Erreur lors du claim de la tache " + task + " pour le user :" + user,
                    e);
        }
    }

    @Override
    public void completeTask(GouvBPMTask task, Integer demandeId) throws Exception {
        LOGGER.info("completeTask({})", task);

        taskService.complete(task.getId());
        
        // Réindexation pour prendre en compte le nouveau statutPublicOuInterne
        reindex(demandeId);
    }

    @Override
    public List<GouvBPMTask> getTasksWhereUserIsCandidate(GouvBPMUser user, String codeAppli) {
        LOGGER.info("getTasksWhereUserIsCandidate({}, {})", user, codeAppli);

        // On transfère le code appli au GouvBPMGroupManager par le biais d'un critère de recherche sur les processVariables
        // Seul moyen de transférer cela au GouvBPMGroupManager, qui a besoin du code appli
        List<Task> tasks = taskService.createTaskQuery()
                .processVariableValueEquals(GouvBPMProcessVariableTypeEnum.MC_CODEAPPLI.name(), codeAppli)
                .taskCandidateUser(user.getId()).list();

        return GouvBPMTransformer.toGouvModelTasks(tasks);
    }

    @Override
    public List<GouvBPMTask> getTasksForDemandeWhereUserIsCandidate(GouvBPMUser user, String codeAppli,
            Integer demandeId) {
        LOGGER.info("getTasksWhereUserIsCandidate({}, {})", user, codeAppli);

        // On transfère le code appli au GouvBPMGroupManager par le biais d'un critère de recherche sur les processVariables
        // Seul moyen de transférer cela au GouvBPMGroupManager, qui a besoin du code appli
        List<Task> tasks = taskService.createTaskQuery()
                .processVariableValueEquals(GouvBPMProcessVariableTypeEnum.MC_CODEAPPLI.name(), codeAppli)
                .processInstanceBusinessKey(demandeId.toString()).taskCandidateUser(user.getId()).list();

        return GouvBPMTransformer.toGouvModelTasks(tasks);
    }

    @Override
    public List<GouvBPMTask> getTasksWhereGroupIsCandidate(GouvBPMGroup group, String codeAppli) {
        LOGGER.info("getTasksWhereGroupIsCandidate({}, {})", group, codeAppli);
        List<Task> tasks = taskService.createTaskQuery()
                .processVariableValueEquals(GouvBPMProcessVariableTypeEnum.MC_CODEAPPLI.name(), codeAppli)
                .taskCandidateGroup(group.getId()).list();
        return GouvBPMTransformer.toGouvModelTasks(tasks);
    }

    private ProcessInstance getActiveProcessInstanceForDemandeId(Integer demandeId) {
        LOGGER.info("getActiveProcessInstanceForDemandeId({})", demandeId);
        return runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(demandeId.toString()).active()
                .singleResult();
    }

    @Override
    public List<Integer> getDemandesIdsByCodeAppliAndTacheCourante(String codeAppli, GouvBPMTask task) {
        LOGGER.info("getDemandesIdsByCodeAppliAndTacheCourante({}, {})", codeAppli, task.getTaskDefinitionKey());
        List<Integer> demandeIds = new ArrayList<>();
        List<Task> tasks = taskService.createTaskQuery()
                .processVariableValueEquals(GouvBPMProcessVariableTypeEnum.MC_CODEAPPLI.name(), codeAppli)
                .taskDefinitionKeyLike(task.getTaskDefinitionKey()).active().list();
        for (Task t : tasks) {
            Integer demandeId = Integer.parseInt(runtimeService.createProcessInstanceQuery()
                    .processInstanceId(t.getProcessInstanceId()).singleResult().getBusinessKey());
            demandeIds.add(demandeId);
        }
        return demandeIds;
    }

    @Override
    public List<Integer> getDemandesIdsByCodeAppliAndTacheCouranteAndCandidateUser(String codeAppli, GouvBPMTask task,
            GouvBPMUser user) {
        LOGGER.info("getDemandesIdsByCodeAppliAndTacheCourante({}, {})", codeAppli, task.getTaskDefinitionKey());
        List<Integer> demandeIds = new ArrayList<>();
        List<Task> tasks = taskService.createTaskQuery()
                .processVariableValueEquals(GouvBPMProcessVariableTypeEnum.MC_CODEAPPLI.name(), codeAppli)
                .taskDefinitionKey(task.getTaskDefinitionKey()).taskCandidateUser(user.getId()).active().list();
        for (Task t : tasks) {
            Integer demandeId = Integer.parseInt(runtimeService.createProcessInstanceQuery()
                    .processInstanceId(t.getProcessInstanceId()).singleResult().getBusinessKey());
            demandeIds.add(demandeId);
        }
        return demandeIds;
    }

    @Override
    public boolean isProcessInstanceAlive(Integer demandeId) {
        ProcessInstance processInstance = getActiveProcessInstanceForDemandeId(demandeId);
        return processInstance != null && !processInstance.isEnded();
    }

    @Override
    public void jump(Integer demandeId, final GouvBPMTask taskFrom, final GouvBPMTask taskTo) {

        LOGGER.info("jump({}, {})", taskFrom.getTaskDefinitionKey(), taskTo.getTaskDefinitionKey());

        final ProcessInstance process = getActiveProcessInstanceForDemandeId(demandeId);

        LOGGER.info("Création de la nouvelle tâche ({})...", taskTo);

        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(demandeId.toString()).singleResult();
        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);
        ProcessDefinitionImpl pdd = ((ExecutionEntity) pi).getProcessDefinition();

        final ActivityImpl activityFinal = pdd.findActivity(taskTo.getTaskDefinitionKey());

        CommandExecutor commandExecutor = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration()
                .getCommandExecutor();
        commandExecutor.execute(commandContext -> {
            ExecutionEntity ee = commandContext.getExecutionEntityManager()
                    .findExecutionById(process.getId());
            ee.executeActivity(activityFinal);
            return null;
        });

        LOGGER.info("Suppression de l'ancienne tâche ({})...", taskFrom);

        Task previousTask = processEngineConfiguration.getTaskService().createTaskQuery()
                .taskDefinitionKey(taskFrom.getTaskDefinitionKey()).singleResult();
        final TaskEntity te = (TaskEntity) previousTask;

        commandExecutor = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration().getCommandExecutor();
        commandExecutor.execute(commandContext -> {
            commandContext.getTaskEntityManager().deleteTask(te, "jump requested", true);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CommentaireInterneDTO> getCommentairesInternes(Integer demandeId) {
        LOGGER.debug("getCommentairesInternes({})", demandeId);
        List<CommentaireInterneDTO> commInternes = new ArrayList<>();
        ProcessInstance processInstance = getActiveProcessInstanceForDemandeId(demandeId);
        if (processInstance != null) {
            commInternes = (List<CommentaireInterneDTO>) runtimeService
                    .getVariable(processInstance.getId(), GouvBPMProcessVariableTypeEnum.MC_COMMINTERNES.name());
            if (commInternes == null) {
                commInternes = new ArrayList<>();
            }
        } else {
            LOGGER.error(NULL_PI);
        }
        return commInternes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void putCommentaireInterne(Integer demandeId, CommentaireInterneDTO commentaire) {
        LOGGER.debug("putCommentaireInterne({}, {})", demandeId, commentaire);
        ProcessInstance processInstance = getActiveProcessInstanceForDemandeId(demandeId);
        if (processInstance != null) {
            List<CommentaireInterneDTO> commInternes = (List<CommentaireInterneDTO>) runtimeService
                    .getVariable(processInstance.getId(), GouvBPMProcessVariableTypeEnum.MC_COMMINTERNES.name());
            if (commInternes == null) {
                commInternes = new ArrayList<>();
            }
            commInternes.add(commentaire);
            runtimeService.setVariable(processInstance.getId(), GouvBPMProcessVariableTypeEnum.MC_COMMINTERNES.name(),
                    commInternes);
        } else {
            LOGGER.error(NULL_PI);
        }
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
    public void submitTaskFormData(GouvBPMTask task, Map<String, String> properties, Integer demandeId) throws Exception {
        // Pour éviter les NPE dans Activiti et éviter d'avoir à déclarer de nouveaux HashMaps
        // si on ne veut rien transmettre dans le formulaire
        Map<String, String> propertiesSafe = (properties == null) ? new HashMap<>() : properties;
        formService.submitTaskFormData(task.getId(), propertiesSafe);
        // Réindexation pour prendre en compte le nouveau statutPublicOuInterne
        reindex(demandeId);
    }

    private void reindex(Integer demandeId) throws IOException, TikaException, SAXException {
        // Réindexation pour prendre en compte le nouveau statutPublicOuInterne
        if (indexedDemandeService != null) {
            indexedDemandeService.indexDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);
        }
    }

    @SuppressWarnings({"unchecked", "java:S2864"})
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
    public void annulerDemande(Integer demandeId, GouvBPMUser agent, GouvBPMUser usager, String codeMotif, String commentaire, String statutAnnulation) {
        LOGGER.info("Annulation de la demande {} par l'agent '{}' ou l'usager {}", demandeId, agent, usager);
        List<Execution> executions = runtimeService.createExecutionQuery()
                .processInstanceBusinessKey(demandeId.toString(), true)
                .messageEventSubscriptionName("annulationMessage").list();

        Map<String, Object> variables = new HashMap<>();
        variables.put(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name(), codeMotif);
        variables.put(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name(), commentaire);
        variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE.name(), statutAnnulation);
        if (usager != null) {
            variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_USAGER.name(), usager.getId());
        } else if (agent != null) {
            variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_AGENT.name(), agent.getId());
        }

        //normalement il y en a une seule

        LOGGER.info("Nombre d'executions candidats à l'annulation pour la demande {} : {}", demandeId,
                executions.size());

        if (executions.isEmpty()) {
            throw new GouvBPMException("Aucune execution pour annuler la demande : " + demandeId);
        }

        for (Execution ex : executions) {
            runtimeService.messageEventReceived("annulationMessage", ex.getId(), variables);
        }
    }

}

package mc.gouv.af.back.bpm.activiti;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.FormService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.bpm.GouvBPM;
import mc.gouv.af.back.bpm.GouvBPMException;
import mc.gouv.af.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.af.back.bpm.model.GouvBPMGroup;
import mc.gouv.af.back.bpm.model.GouvBPMStatutAction;
import mc.gouv.af.back.bpm.model.GouvBPMTask;
import mc.gouv.af.back.bpm.model.GouvBPMUser;
import mc.gouv.af.back.util.CommentaireInterneDTO;
import mc.gouv.dem.apishared.model.DemandeStatutEnum;

/**
 * Composant exposant le BPM interne d'AppFactory
 * 
 * @author qdeme
 *
 */
@Component
public class GouvBPMImpl implements GouvBPM {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMImpl.class);

    @Autowired
    private RuntimeService runtimeService;
    
    @Autowired
    private IdentityService identityService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;
    
    @Autowired
    private ProcessEngine processEngine;
    
    @Autowired
    private FormService formService;
    
    @Override
    public void startProcessInstance(String processDefinitionKey, GouvBPMUser user, Integer demandeId, String codeAppli, Map<String, Object> businessVariables) {
        LOGGER.info("startProcessInstance() Démarrage d'une instance du process \"" + processDefinitionKey + "\" assignée à l'utilisateur \"" + user + "\" et concernant la demande \"" + demandeId + "\"");
        
        checkUser(user);
        
        // Création des variables du process
        Map<String, Object> variables = new HashMap<String, Object>();
        // Variables techniques
        variables.put(GouvBPMProcessVariableTypeEnum.MC_USERID.name(), user.getId());
        variables.put(GouvBPMProcessVariableTypeEnum.MC_CODEAPPLI.name(), codeAppli);
        // Variables business éventuellement fournies par le client
        if (businessVariables != null) {
            variables.putAll(businessVariables);
        }
        
        ProcessInstance process = null;
        try {
            // On utilise le demandeId pour la "businessKey"
            process = runtimeService.startProcessInstanceByKey(processDefinitionKey, demandeId.toString(), variables);
            LOGGER.info("Instance démarrée : " + process.getDeploymentId() + " , " + process.getActivityId() + " , " + process.getId() + " , "  + process.getDescription() + " , " + process.getProcessInstanceId());
        }
        catch (ActivitiObjectNotFoundException e) {
            throw new GouvBPMException("Erreur lors du démarrage de l'instance de process", e);
        }

    }

    @Override
    public Map<String, Object> getProcessBusinessVariables(Integer demandeId) {
        LOGGER.debug("getProcessBusinessVariables(" + demandeId + ")");
        ProcessInstance processInstance = getActiveProcessInstanceForDemandeId(demandeId);
        if (processInstance == null) {
            LOGGER.error("ProcessInstance null !");
            return null;
        }
        return runtimeService.getVariables(processInstance.getId());
    }

    @Override
    public void setProcessBusinessVariables(Integer demandeId, Map<String, Object> businessVariables) {
        LOGGER.debug("setProcessBusinessVariables(" + demandeId + ")");
        ProcessInstance processInstance = getActiveProcessInstanceForDemandeId(demandeId);
        if (processInstance != null) {
            runtimeService.setVariables(processInstance.getId(), businessVariables);
        }
        else {
            LOGGER.error("ProcessInstance null !");
        }
    }
    
    @Override
    public List<GouvBPMTask> getTasksAssignedToUser(GouvBPMUser user) {
        LOGGER.debug("getTasksAssignedToUser(" + user  +")");
        
        checkUser(user);
        
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(user.getId()).active().list();
        return GouvBPMTransformer.toGouvModelTasks(tasks);
    }

    @Override
    public List<GouvBPMTask> getActiveTasksForDemande(Integer demandeId) {
        LOGGER.debug("getActiveTasksForDemande(" + demandeId + ")");
        
        List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(demandeId.toString()).active().list();
        return GouvBPMTransformer.toGouvModelTasks(tasks);
    }

    @Override
    public void claimTask(GouvBPMTask task, GouvBPMUser user) {
        LOGGER.info("claimTask(" + task + "," + user + ")");
        
        checkUser(user);
        
        taskService.claim(task.getId(), user.getId());
    }

    @Override
    public void completeTask(GouvBPMTask task) {
        LOGGER.info("completeTask(" + task + ")");
        
        taskService.complete(task.getId());
    }

    @Override
    public List<GouvBPMTask> getTasksWhereUserIsCandidate(GouvBPMUser user, String codeAppli) {
        LOGGER.info("getTasksWhereUserIsCandidate(" + user + "," + codeAppli + ")");
        
        // À la fin c'est logonProxy.getUserByMatricule() du GouvBPMGroupManager qui est utilisé, donc inutile
        // de vérifier l'utilisateur avant puisque c'est fait après pour trouver ses groupes
        // checkUser(user);

        // On transfère le code appli au GouvBPMGroupManager par le biais d'un critère de recherche sur les processVariables
        // Seul moyen de transférer cela au GouvBPMGroupManager, qui a besoin du code appli
        List<Task> tasks = taskService.createTaskQuery()
                .processVariableValueEquals(GouvBPMProcessVariableTypeEnum.MC_CODEAPPLI.name(), codeAppli)
                .taskCandidateUser(user.getId()).list();
        
        return GouvBPMTransformer.toGouvModelTasks(tasks);
    }
    
    @Override
    public List<GouvBPMTask> getTasksForDemandeWhereUserIsCandidate(GouvBPMUser user, String codeAppli, Integer demandeId) {
        LOGGER.info("getTasksWhereUserIsCandidate(" + user + "," + codeAppli + ")");
        
        // À la fin c'est logonProxy.getUserByMatricule() du GouvBPMGroupManager qui est utilisé, donc inutile
        // de vérifier l'utilisateur avant puisque c'est fait après pour trouver ses groupes
        // checkUser(user);

        // On transfère le code appli au GouvBPMGroupManager par le biais d'un critère de recherche sur les processVariables
        // Seul moyen de transférer cela au GouvBPMGroupManager, qui a besoin du code appli
        List<Task> tasks = taskService.createTaskQuery()
                .processVariableValueEquals(GouvBPMProcessVariableTypeEnum.MC_CODEAPPLI.name(), codeAppli)
                .processInstanceBusinessKey(demandeId.toString())
                .taskCandidateUser(user.getId()).list();
        
        return GouvBPMTransformer.toGouvModelTasks(tasks);
    }
    
    @Override
    public List<GouvBPMTask> getTasksWhereGroupIsCandidate(GouvBPMGroup group, String codeAppli) {
        LOGGER.info("getTasksWhereGroupIsCandidate(" + group + "," + codeAppli + ")");
        
        checkGroup(group, codeAppli);

        List<Task> tasks = taskService.createTaskQuery()
                .processVariableValueEquals(GouvBPMProcessVariableTypeEnum.MC_CODEAPPLI.name(), codeAppli)
                .taskCandidateGroup(group.getId()).list();
        return GouvBPMTransformer.toGouvModelTasks(tasks);
    }

    private ProcessInstance getActiveProcessInstanceForDemandeId(Integer demandeId) {
        LOGGER.info("getActiveProcessInstanceForDemandeId(" + demandeId + ")");
        return runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(demandeId.toString()).active().singleResult();
    }
    
    @Override
    public List<Integer> getDemandesIdsByCodeAppliAndTacheCourante(String codeAppli, GouvBPMTask task) {
        LOGGER.info("getDemandesIdsByCodeAppliAndTacheCourante(" + codeAppli + "," + task.getTaskDefinitionKey() + ")");
        List<Integer> demandeIds = new ArrayList<Integer>();
        List<Task> tasks = taskService.createTaskQuery()
                .processVariableValueEquals(GouvBPMProcessVariableTypeEnum.MC_CODEAPPLI.name(), codeAppli)
                .taskDefinitionKey(task.getTaskDefinitionKey()).active().list();
        for (Task t : tasks) {
            Integer demandeId = Integer.parseInt(runtimeService.createProcessInstanceQuery()
                    .processInstanceId(t.getProcessInstanceId()).singleResult().getBusinessKey());
            demandeIds.add(demandeId);
        }
        return demandeIds;
    }
    
    @Override
    public List<Integer> getDemandesIdsByCodeAppliAndTacheCouranteAndCandidateUser(String codeAppli, GouvBPMTask task, GouvBPMUser user) {
        LOGGER.info("getDemandesIdsByCodeAppliAndTacheCourante(" + codeAppli + "," + task.getTaskDefinitionKey() + ")");
        List<Integer> demandeIds = new ArrayList<Integer>();
        List<Task> tasks = taskService.createTaskQuery()
                .processVariableValueEquals(GouvBPMProcessVariableTypeEnum.MC_CODEAPPLI.name(), codeAppli)
                .taskDefinitionKey(task.getTaskDefinitionKey())
                .taskCandidateUser(user.getId())
                .active().list();
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
        
        LOGGER.info("jump(" + taskFrom.getTaskDefinitionKey() + "," + taskTo.getTaskDefinitionKey() + ")");
        
        final ProcessInstance process = getActiveProcessInstanceForDemandeId(demandeId);
        
        LOGGER.info("Création de la nouvelle tâche (" + taskTo + ")...");
        
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(demandeId.toString()).singleResult();
        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl)processEngineConfiguration);
        ProcessDefinitionImpl pdd = ((ExecutionEntity)pi).getProcessDefinition();
        ActivityImpl activity = pdd.findActivity(taskTo.getTaskDefinitionKey());
        
        final ProcessInstance processInstanceFinal = process;
        final ActivityImpl activityFinal = activity;

        CommandExecutor commandExecutor = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration()
                .getCommandExecutor();
        commandExecutor.execute(new Command<Void>() {
            public Void execute(CommandContext commandContext) {
                ExecutionEntity ee = commandContext.getExecutionEntityManager()
                        .findExecutionById(processInstanceFinal.getId());
                ee.executeActivity(activityFinal);
                return null;
            }
        });

        LOGGER.info("Suppression de l'ancienne tâche (" + taskFrom + ")...");
        
        Task previousTask = processEngineConfiguration.getTaskService().createTaskQuery()
                .taskDefinitionKey(taskFrom.getTaskDefinitionKey()).singleResult();
        final TaskEntity te = (TaskEntity) previousTask;

        commandExecutor = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration().getCommandExecutor();
        commandExecutor.execute(new Command<Void>() {
            public Void execute(CommandContext commandContext) {
                commandContext.getTaskEntityManager().deleteTask(te, "jump requested", true);
                return null;
            }
        });
    }
    
    /**
     * Vérification de l'existence de l'utilisateur donné en paramètre
     * @param user
     */
    private void checkUser(GouvBPMUser user) {
        LOGGER.info("checkUser(" + user + ")");
        User activitiUser = identityService.createUserQuery().userId(user.getId()).singleResult();
        if (activitiUser == null) {
            LOGGER.info("User inexistant");
            // TODO REVERT
            //throw new GouvBPMException("Utilisateur " + user + " non reconnu");
        }
    }
    
    /**
     * Vérification de l'existence du groupe donné en paramètre
     * @param group
     */
    private void checkGroup(GouvBPMGroup group, String codeAppli) {
        LOGGER.info("checkGroup(" + group + "," + codeAppli + ")");
        // HACK On utilise un critère "groupType" pour faire passer l'info du code appli...
        Group activitiGroup = identityService.createGroupQuery().groupId(group.getId()).groupType(codeAppli).singleResult();
        if (activitiGroup == null) {
            LOGGER.info("Groupe inexistant");
            // TODO REVERT
            //throw new GouvBPMException("Groupe " + group + " non reconnu");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CommentaireInterneDTO> getCommentairesInternes(Integer demandeId) {
        LOGGER.debug("getCommentairesInternes(" + demandeId + ")");
        ProcessInstance processInstance = getActiveProcessInstanceForDemandeId(demandeId);
        if (processInstance != null) {
            List<CommentaireInterneDTO> commInternes = (List<CommentaireInterneDTO>)runtimeService.getVariable(processInstance.getId(), GouvBPMProcessVariableTypeEnum.MC_COMMINTERNES.name());
            if (commInternes == null) {
                commInternes = new ArrayList<CommentaireInterneDTO>();
            }
            return commInternes;
        }
        else {
            LOGGER.error("ProcessInstance null !");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void putCommentaireInterne(Integer demandeId, CommentaireInterneDTO commentaire) {
        LOGGER.debug("putCommentaireInterne(" + demandeId + "," + commentaire+ ")");
        ProcessInstance processInstance = getActiveProcessInstanceForDemandeId(demandeId);
        if (processInstance != null) {
            List<CommentaireInterneDTO> commInternes = (List<CommentaireInterneDTO>)runtimeService.getVariable(processInstance.getId(), GouvBPMProcessVariableTypeEnum.MC_COMMINTERNES.name());
            if (commInternes == null) {
                commInternes = new ArrayList<CommentaireInterneDTO>();
            }
            commInternes.add(commentaire);
            runtimeService.setVariable(processInstance.getId(), GouvBPMProcessVariableTypeEnum.MC_COMMINTERNES.name(), commInternes);
        }
        else {
            LOGGER.error("ProcessInstance null !");
        }
    }
    
    @Override
    public void submitTaskFormData(GouvBPMTask task, Map<String, String> properties) {
        // Pour éviter les NPE dans Activiti et éviter d'avoir à déclarer de nouveaux HashMaps
        // si on ne veut rien transmettre dans le formulaire
        if (properties == null) {
            properties = new HashMap<String, String>();
        }
        formService.submitTaskFormData(task.getId(), properties);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<GouvBPMStatutAction> getTaskStatutActions(GouvBPMTask task) {
        
        List<GouvBPMStatutAction> statutActions = new ArrayList<GouvBPMStatutAction>();
        List<FormProperty> formProps = formService.getTaskFormData(task.getId()).getFormProperties();
        
        // Isoler le FormProperty correspondant à MC_TARGETSTATE
        for (FormProperty formProp : formProps) {
            if (formProp.getId().equals(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE.name())) {
                Map<String, String> map = (Map<String, String>)formProp.getType().getInformation("values");
                // Lister les valeurs de l'enum et créer les objets faisant l'association action / statut cible
                for (String key : map.keySet()) {
                    statutActions.add(new GouvBPMStatutAction(DemandeStatutEnum.valueOf(key), map.get(key)));
                }
            }
        }
        
        return statutActions;
    }
}

package mc.gouv.af.back.bpm.activiti;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.task.Task;

import mc.gouv.af.back.bpm.model.GouvBPMGroup;
import mc.gouv.af.back.bpm.model.GouvBPMTask;
import mc.gouv.af.back.bpm.model.GouvBPMUser;

/**
 * Transformer pour les classes métier du gouvernement vs. celles d'Activiti
 * 
 * @author qdeme
 *
 */
public class GouvBPMTransformer {
    
    public static GouvBPMTask toGouvModelTask(Task task) {
        GouvBPMTask gouvTask = new GouvBPMTask();
        gouvTask.setId(task.getId());
        gouvTask.setName(task.getName());
        gouvTask.setAssignee(task.getAssignee());
        gouvTask.setTaskDefinitionKey(task.getTaskDefinitionKey());
        return gouvTask;
    }
    
    public static List<GouvBPMTask> toGouvModelTasks(List<Task> tasks) {
        ArrayList<GouvBPMTask> gouvTasks = new ArrayList<GouvBPMTask>();
        for (Task task : tasks) {
            gouvTasks.add(toGouvModelTask(task));
        }
        return gouvTasks;
    }
    
    public static GouvBPMUser toGouvModelUser(User user) {
        GouvBPMUser gouvUser = new GouvBPMUser();
        gouvUser.setId(user.getId());
        gouvUser.setFirstName(user.getFirstName());
        gouvUser.setLastName(user.getLastName());
        gouvUser.setEmail(user.getEmail());
        gouvUser.setPassword(user.getPassword());
        return gouvUser;
    }
    
    public static List<GouvBPMUser> toGouvModelUsers(List<User> users) {
        ArrayList<GouvBPMUser> gouvUsers = new ArrayList<GouvBPMUser>();
        for (User user : users) {
            gouvUsers.add(toGouvModelUser(user));
        }
        return gouvUsers;
    }
    
    public static GouvBPMGroup toGouvModelGroup(Group group) {
        GouvBPMGroup gouvGroup = new GouvBPMGroup();
        gouvGroup.setId(group.getId());
        gouvGroup.setName(group.getName());
        gouvGroup.setType(group.getType());
        return gouvGroup;
    }
    
    public static List<GouvBPMGroup> toGouvModelGroups(List<Group> groups) {
        ArrayList<GouvBPMGroup> gouvGroups = new ArrayList<GouvBPMGroup>();
        for (Group group : groups) {
            gouvGroups.add(toGouvModelGroup(group));
        }
        return gouvGroups;
    }

}

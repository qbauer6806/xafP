package mc.gouv.xaf.back.bpm.activiti;

import java.util.ArrayList;
import java.util.List;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import org.flowable.task.api.Task;

/**
 * Transformer pour les classes métier du gouvernement vs. celles d'Activiti
 *
 * @author qdeme
 */
public class GouvBPMTransformer {

    private GouvBPMTransformer() {
        throw new IllegalStateException("Utility class");
    }

    public static GouvBPMTask toGouvModelTask(Task task) {
        GouvBPMTask gouvTask = new GouvBPMTask();
        gouvTask.setId(task.getId());
        gouvTask.setName(task.getName());
        gouvTask.setAssignee(task.getAssignee());
        gouvTask.setTaskDefinitionKey(task.getTaskDefinitionKey());
        return gouvTask;
    }

    public static List<GouvBPMTask> toGouvModelTasks(List<Task> tasks) {
        ArrayList<GouvBPMTask> gouvTasks = new ArrayList<>();
        for (Task task : tasks) {
            gouvTasks.add(toGouvModelTask(task));
        }
        return gouvTasks;
    }

}

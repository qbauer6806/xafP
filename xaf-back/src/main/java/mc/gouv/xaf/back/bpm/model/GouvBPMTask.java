package mc.gouv.xaf.back.bpm.model;

/**
 * Représente une tâche du BPM du gouvernement
 * 
 * @author qdeme
 *
 */
public class GouvBPMTask {

    private String id;
    
    private String name;
    
    private String assignee;
    
    private String taskDefinitionKey;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

    @Override
    public String toString() {
        return "GouvBPMTask [id=" + id + ", name=" + name + ", assignee=" + assignee + ", taskDefinitionKey="
                + taskDefinitionKey + "]";
    }

}

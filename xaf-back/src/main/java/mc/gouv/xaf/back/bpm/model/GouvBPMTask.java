package mc.gouv.xaf.back.bpm.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Représente une tâche du BPM du gouvernement
 *
 * @author qdeme
 */
@ToString
@Setter
@Getter
public class GouvBPMTask {

    private String id;

    private String name;

    private String assignee;

    private String taskDefinitionKey;

}

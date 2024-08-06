package mc.gouv.xaf.back.bpm.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Représente un groupe du BPM du gouvernement
 * 
 * @author qdeme
 *
 */
@ToString
@Setter
@Getter
public class GouvBPMGroup {
    
    private String id;
    
    private String name;
    
    private String type;
    
}

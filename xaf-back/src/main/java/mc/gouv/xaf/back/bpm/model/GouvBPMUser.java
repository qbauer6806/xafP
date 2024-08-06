package mc.gouv.xaf.back.bpm.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Représente un utilisateur du BPM du gouvernement
 * 
 * @author qdeme
 *
 */
@ToString
@Setter
@Getter
public class GouvBPMUser {

    private String id;
    
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private String password;
    
}

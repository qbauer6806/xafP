package mc.gouv.af.back.bpm.model;

/**
 * Représente un utilisateur du BPM du gouvernement
 * 
 * @author qdeme
 *
 */
public class GouvBPMUser {

    private String id;
    
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private String password;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "GouvBPMUser [id=" + id + ", email=" + email + ", firstName=" + firstName + ", lastName=" + lastName
                + "]";
    }
    
}

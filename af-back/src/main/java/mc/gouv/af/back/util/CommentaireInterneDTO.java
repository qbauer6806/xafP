package mc.gouv.af.back.util;

import java.io.Serializable;
import java.util.Date;

public class CommentaireInterneDTO implements Serializable {

    private static final long serialVersionUID = -3630330943792014082L;

    private String agentId;
    
    private Date date;
    
    private String commentaire;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
    
}

package mc.gouv.af.back.bpm.model;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>Classe représentant un commentaire interne entre intervenants du back-office</p>
 * <p>Important: les deux classes CommentaireInterneDTO doivent rester présente. Le BPMN a besoin des deux classes pour déserialisé les objets.</p>
 *
 * @author qdeme
 */
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

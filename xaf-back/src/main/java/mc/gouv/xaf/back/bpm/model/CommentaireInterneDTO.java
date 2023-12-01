package mc.gouv.xaf.back.bpm.model;

import java.util.Date;

/**
 * <p>Classe représentant un commentaire interne entre intervenants du back-office</p>
 * <p>Important: les deux classes CommentaireInterneDTO doivent rester présentes. Le BPMN a besoin des deux classes pour déserialiser les objets.</p>
 *
 * @author qdeme
 */
public class CommentaireInterneDTO {

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

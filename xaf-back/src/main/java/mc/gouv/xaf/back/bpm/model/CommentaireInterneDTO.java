package mc.gouv.xaf.back.bpm.model;

import java.util.Date;

/**
 * <p>Classe représentant un commentaire interne entre intervenants du back-office</p>
 * <p>Important: les deux classes CommentaireInterneDTO doivent rester présentes. Le BPMN a besoin des deux classes pour déserialiser les objets.</p>
 *
 * @author qdeme
 */
// TODO : Supprimer le doublon de cette classe (voir ticket #20760)
public class CommentaireInterneDTO extends mc.gouv.af.back.bpm.model.CommentaireInterneDTO {

    private static final long serialVersionUID = -3630330943792014082L;

    private String agentId;

    private Date date;

    private String commentaire;

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String getCommentaire() {
        return commentaire;
    }

    @Override
    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}

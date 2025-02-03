package mc.gouv.af.back.bpm.model;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>Classe représentant un commentaire interne entre intervenants du back-office</p>
 * <p>Important: les deux classes CommentaireInterneDTO doivent rester présentes. Le BPMN a besoin des deux classes pour
 * déserialiser les objets.</p>
 *
 * @author qdeme
 */
@Setter
@Getter
public class CommentaireInterneDTO implements Serializable {

    private String agentId;

    private Date date;

    private String commentaire;

}

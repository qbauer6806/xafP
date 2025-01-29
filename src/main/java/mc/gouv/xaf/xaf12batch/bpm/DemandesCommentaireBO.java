package mc.gouv.xaf.xaf12batch.bpm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.xaf12batch.dto.DemandeBO;

@Setter
@Getter
@Entity
@Table(name = "DEM_DEMANDES_COMMENTAIRE")
public class DemandesCommentaireBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDESCOMMENTAIRE", nullable = false)
    private Integer pkDemandesCommentaire;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDES", nullable = false)
    private DemandeBO fkDemandes;

    @Column(name = "DATE")
    private Date date;

    @Column(name = "AGENT_ID", length = 128)
    @Size(max = 128)
    private String agentId;

    @Column(name = "COMMENTAIRE")
    private String commentaire;

}

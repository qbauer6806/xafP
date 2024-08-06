package mc.gouv.xaf.back.data.entity;

import jakarta.validation.constraints.NotBlank;

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

/**
 * 
 * Classe BO de la table DEM.STATUTS
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_DEMANDES_STATUTS")
public class DemandesStatutsBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDESSTATUTS", nullable = false)
    private Integer pkDemandesStatuts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_DEMANDES")
    private DemandeBO fkDemandes;

    @Column(name = "LIBELLE", length = 128)
    @Size(max = 128)
    private String libelle;

    @Column(name = "NAME", length = 64, nullable = false)
    @NotBlank
    @Size(min = 1, max = 64)
    private String name;

    @Column(name = "DATE", nullable = false)
    private Date date;

    @Column(name = "AGENT_ID", length = 128)
    @Size(max = 128)
    private String agentId;

    @Column(name = "USAGER_ID")
    private Integer usagerId;

    @Column(name = "CODE_MOTIF", length = 128)
    @Size(max = 128)
    private String codeMotif;

    @Column(name = "COMMENTAIRE", length = 8000)
    @Size(max = 8000)
    private String commentaire;

    @Column(name = "TEXTE_A_ENVOYER", columnDefinition = "TEXT")
    private String texteAEnvoyer;

    @Override
    public String toString() {
        return "DemandesStatutsBO [libelle=" + libelle + ", date=" + date + ", agentId=" + agentId + ", usagerId="
                + usagerId + ", codeMotif=" + codeMotif + ", commentaire=" + commentaire+ ", textAEnvoyer=" + texteAEnvoyer + "]";
    }

}

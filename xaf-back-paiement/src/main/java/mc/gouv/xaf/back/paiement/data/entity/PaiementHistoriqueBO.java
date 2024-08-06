package mc.gouv.xaf.back.paiement.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.data.entity.DemandeBO;

/**
 * Classe BO de la table PMNT_HISTORIQUE
 *
 * @author mboutelier.ext
 */
@Setter
@Getter
@Entity
@Table(name = "PMNT_HISTORIQUE")
public class PaiementHistoriqueBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_HISTORIQUE", nullable = false)
    private Integer pkHistorique;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDES", nullable = false)
    private DemandeBO fkDemandes;

    @Column(name = "DATE", nullable = false)
    private Timestamp date;

    @Column(name = "STATUT", nullable = false)
    @Size(min = 1, max = 255)
    private String statut;

    @Column(name = "USAGER_ID")
    private Integer usagerId;

    @Column(name = "CONTENU", nullable = false)
    @NotBlank
    @Size(min = 1, max = 10000)
    private String contenu;

}

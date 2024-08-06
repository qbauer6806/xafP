package mc.gouv.xaf.back.data.entity;

import java.util.Date;

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

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Classe BO de la table DEM.HISTORIQUE
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_DEMANDES_HISTORIQUE")
public class DemandesHistoriqueBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDESHISTORIQUE", nullable = false)
    private Integer pkDemandesHistorique;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDES", nullable = false)
    private DemandeBO fkDemandes;

    @Column(name = "DATE", nullable = false)
    private Date date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_STATUT", nullable = false)
    private DemandesStatutsBO fkStatut;

    @Column(name = "AGENT_ID", length = 128)
    @Size(max = 128)
    private String agentId;

    @Column(name = "USAGER_ID")
    private Integer usagerId;

    @Column(name = "JUSTIFICATIF_TRAITEMENT", length = 8000)
    private String justificatifTraitement;

    @Column(name = "CONTENU", length = 10000, nullable = false)
    @NotBlank
    @Size(min = 1, max = 10000)
    private String contenu;

}

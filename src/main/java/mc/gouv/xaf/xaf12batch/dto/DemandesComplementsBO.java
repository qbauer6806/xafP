package mc.gouv.xaf.xaf12batch.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe BO de la table DEM.DEMANDES_COMPLEMENTS
 *
 * @author qdeme
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_DEMANDES_COMPLEMENTS")
public class DemandesComplementsBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDESCOMPLEMENTS", nullable = false)
    private Integer pkDemandesComplements;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDES")
    private DemandeBO fkDemandes;

}


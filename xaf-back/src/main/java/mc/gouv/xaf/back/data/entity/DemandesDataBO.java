package mc.gouv.xaf.back.data.entity;

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
 * Classe BO de la table DEM.DEMANDES_DATA
 *
 * @author qdeme
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_DEMANDES_DATA")
public class DemandesDataBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDESDATA", nullable = false)
    private Integer pkDemandesData;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDES")
    private DemandeBO fkDemandes;

    @Column(name = "KEY", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String key;

    @Column(name = "VALUE", length = 10000)
    @Size(max = 10000)
    private String value;

}

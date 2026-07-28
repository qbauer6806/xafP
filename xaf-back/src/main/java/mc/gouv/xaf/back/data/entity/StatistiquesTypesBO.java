package mc.gouv.xaf.back.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe BO de la table DEM.STATISTQUES.TYPES
 *
 * @author xdecool
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_STATISTIQUES_TYPES")
public class StatistiquesTypesBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_STATISTIQUES_TYPES", nullable = false)
    private Integer pkStatistiquesTypes;

    @Column(name = "IDENTIFIANT_DEMANDE", length = 30)
    @Size(min = 1, max = 30)
    private String identifiantDemande;

    @Column(name = "VALUE", length = 250)
    @Size(min = 1, max = 250)
    private String value;

}

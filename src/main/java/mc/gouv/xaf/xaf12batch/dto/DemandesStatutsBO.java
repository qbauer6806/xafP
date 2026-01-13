package mc.gouv.xaf.xaf12batch.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Classe BO de la table DEM.STATUTS
 *
 * @author qdeme
 */
@Setter
@Getter
@ToString
@Entity
@Table(name = "DEM_DEMANDES_STATUTS")
public class DemandesStatutsBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDESSTATUTS", nullable = false)
    private Integer pkDemandesStatuts;

    @Column(name = "NAME", length = 64, nullable = false)
    @NotBlank
    @Size(min = 1, max = 64)
    private String name;

}

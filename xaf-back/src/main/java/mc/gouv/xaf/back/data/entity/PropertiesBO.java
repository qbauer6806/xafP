package mc.gouv.xaf.back.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe BO de la table DEM.PROPERTIES
 *
 * @author mboutelier.ext
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_PROPERTIES")
public class PropertiesBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_PROPERTIES", nullable = false)
    private Integer pkProperties;

    @Column(name = "TYPE", length = 256, nullable = false)
    @NotEmpty
    @Size(min = 1, max = 256)
    private String type;

    @Column(name = "KEY", length = 256, nullable = false)
    @NotEmpty
    @Size(min = 1, max = 256)
    private String key;

    @Column(name = "DESCRIPTIF", length = 256)
    @Size(max = 256)
    private String descriptif;

    @Column(name = "VALUE", columnDefinition = "TEXT")
    private String value;

}

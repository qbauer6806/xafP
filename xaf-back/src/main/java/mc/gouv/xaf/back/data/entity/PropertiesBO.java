package mc.gouv.xaf.back.data.entity;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * Classe BO de la table DEM.PROPERTIES
 *
 * @author mboutelier.ext
 */
@Entity
@Table(name = "DEM_PROPERTIES")
public class PropertiesBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_PROPERTIES", nullable = false)
    private Integer pkProperties;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMARCHEID", nullable = false)
    private DemarchesBO demarche;

    @Column(name = "TYPE", length = 256, nullable = false)
    @NotEmpty
    @Size(min = 1, max = 256)
    private String type;

    @Column(name = "\"KEY\"", length = 256, nullable = false)
    @NotEmpty
    @Size(min = 1, max = 256)
    private String key;

    @Column(name = "DESCRIPTIF", length = 256)
    @Size(min = 0, max = 256)
    private String descriptif;

    @Column(name = "\"VALUE\"", columnDefinition = "TEXT")
    private String value;

    public Integer getPkProperties() {
        return pkProperties;
    }

    public void setPkProperties(Integer pkProperties) {
        this.pkProperties = pkProperties;
    }

    public DemarchesBO getDemarche() {
        return demarche;
    }

    public void setDemarche(DemarchesBO demarche) {
        this.demarche = demarche;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescriptif() {
        return descriptif;
    }

    public void setDescriptif(String descriptif) {
        this.descriptif = descriptif;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

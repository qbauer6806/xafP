package mc.gouv.xaf.back.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * Classe BO de la table DEM.DEMANDES_DATA
 * 
 * @author qdeme
 *
 */
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

    @Column(name = "VALUE", length = 10000, nullable = true)
    @Size(min = 0, max = 10000)
    private String value;

    public Integer getPkDemandesData() {
        return pkDemandesData;
    }

    public void setPkDemandesData(Integer pkDemandesData) {
        this.pkDemandesData = pkDemandesData;
    }

    public DemandeBO getFkDemandes() {
        return fkDemandes;
    }

    public void setFkDemandes(DemandeBO fkDemandes) {
        this.fkDemandes = fkDemandes;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}

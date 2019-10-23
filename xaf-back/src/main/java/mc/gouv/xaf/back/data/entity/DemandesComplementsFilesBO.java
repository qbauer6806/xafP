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
 * Classe BO de la table DEM.DEMANDES_COMPLEMENTS_FILES
 * 
 * @author qdeme
 *
 */
@Entity
@Table(name = "DEM_DEMANDES_COMPLEMENTS_FILES")
public class DemandesComplementsFilesBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDESCOMPLEMENTSFILES", nullable = false)
    private Integer pkDemandesComplementsFiles;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDESCOMPLEMENTS")
    private DemandesComplementsBO fkDemandesComplements;

    @Column(name = "NAME", length = 1024, nullable = false)
    @NotBlank
    @Size(min = 1, max = 1024)
    private String name;

    @Column(name = "URL", length = 1024, nullable = false)
    @NotBlank
    @Size(min = 1, max = 1024)
    private String url;

    @Column(name = "meta", length = 512, nullable = true)
    @Size(min = 0, max = 512)
    private String meta;

    public Integer getPkDemandesComplementsFiles() {
        return pkDemandesComplementsFiles;
    }

    public void setPkDemandesComplementsFiles(Integer pkDemandesComplements) {
        this.pkDemandesComplementsFiles = pkDemandesComplements;
    }

    public DemandesComplementsBO getFkDemandesComplements() {
        return fkDemandesComplements;
    }

    public void setFkDemandesComplements(DemandesComplementsBO fkDemandesComplements) {
        this.fkDemandesComplements = fkDemandesComplements;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }
    
}

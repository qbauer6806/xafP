package mc.gouv.xaf.back.data.entity;

import java.util.Date;

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
 * Classe BO de la table DEM.DEMANDES_FILES
 * 
 * @author qdeme
 *
 */
@Entity
@Table(name = "DEM_DEMANDES_FILES")
public class DemandesFilesBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDESFILES", nullable = false)
    private Integer pkDemandesFiles;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDES")
    private DemandeBO fkDemandes;

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
    
    @Column(name = "DATE", nullable = true)
    private Date date;

    public Integer getPkDemandesFiles() {
        return pkDemandesFiles;
    }

    public void setPkDemandesFiles(Integer pkDemandesFiles) {
        this.pkDemandesFiles = pkDemandesFiles;
    }

    public DemandeBO getFkDemandes() {
        return fkDemandes;
    }

    public void setFkDemandes(DemandeBO fkDemandes) {
        this.fkDemandes = fkDemandes;
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}

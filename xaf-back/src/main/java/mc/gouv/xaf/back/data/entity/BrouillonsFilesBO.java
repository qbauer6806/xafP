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
 * Classe BO de la table DEM.BROUILLONS_FILES
 * 
 * @author qdeme
 *
 */
@Entity
@Table(name = "DEM_BROUILLONS_FILES")
public class BrouillonsFilesBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_BROUILLONSFILES", nullable = false)
    private Integer pkBrouillonsFiles;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_BROUILLONS")
    private BrouillonBO fkBrouillons;

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
    
    @Column(name = "TYPEDOC", length = 128)
    private String typedoc;

	public Integer getPkBrouillonsFiles() {
		return pkBrouillonsFiles;
	}

	public void setPkBrouillonsFiles(Integer pkBrouillonsFiles) {
		this.pkBrouillonsFiles = pkBrouillonsFiles;
	}

	public BrouillonBO getFkBrouillons() {
		return fkBrouillons;
	}

	public void setFkBrouillons(BrouillonBO fkBrouillons) {
		this.fkBrouillons = fkBrouillons;
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

	public String getTypedoc() {
		return typedoc;
	}

	public void setTypedoc(String typedoc) {
		this.typedoc = typedoc;
	}

}

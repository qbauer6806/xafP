package mc.gouv.xaf.back.data.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * Classe BO de la table DEM.BROUILLONS
 * 
 * @author qdeme
 *
 */
@Entity
@Table(name = "DEM_BROUILLONS")
public class BrouillonBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_BROUILLONS", nullable = false)
    private Integer pkBrouillons;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_ACCESS")
    private AccessBO fkAccess;

    @Column(name = "DATE_CREATION", nullable = false)
    private Date dateCreation;

    @Column(name = "DATE_DERMODIF", nullable = false)
    private Date dateDerModif;

    @Column(name = "CONTENU", columnDefinition = "TEXT", nullable = false)
    @NotBlank
    private String contenu;

    @OneToMany(mappedBy = "fkBrouillons", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<BrouillonsFilesBO> files;

    @Column(name = "BUILD_ID", length = 32, nullable = true)
    @Size(min = 0, max = 32)
    private String buildId;

    @Column(name = "RECAP_TYPE", length = 256, nullable = true)
    @Size(min = 0, max = 256)
    private String recapType;

    @Column(name = "META", columnDefinition = "TEXT", nullable = true)
    private String meta;

    @Column(name = "CONTENU_INITIAL", columnDefinition = "TEXT", nullable = true)
    private String contenuInitial;

    public Integer getPkBrouillons() {
        return pkBrouillons;
    }

    public void setPkBrouillons(Integer pkBrouillons) {
        this.pkBrouillons = pkBrouillons;
    }

    public AccessBO getFkAccess() {
        return fkAccess;
    }

    public void setFkAccess(AccessBO fkAccess) {
        this.fkAccess = fkAccess;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Date getDateDerModif() {
        return dateDerModif;
    }

    public void setDateDerModif(Date dateDerModif) {
        this.dateDerModif = dateDerModif;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Set<BrouillonsFilesBO> getFiles() {
        return files;
    }

    public void setFiles(Set<BrouillonsFilesBO> files) {
        this.files = files;
    }

    public String getBuildId() {
        return buildId;
    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    public String getRecapType() {
        return recapType;
    }

    public void setRecapType(String recapType) {
        this.recapType = recapType;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public String getContenuInitial() {
        return contenuInitial;
    }

    public void setContenuInitial(String contenuInitial) {
        this.contenuInitial = contenuInitial;
    }

}

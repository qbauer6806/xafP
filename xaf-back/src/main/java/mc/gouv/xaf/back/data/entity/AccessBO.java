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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * Classe BO de la table DEM.ACCESS
 * 
 * @author qdeme
 *
 */
@Entity
@Table(name = "DEM_ACCESS")
public class AccessBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_ACCESS", nullable = false)
    private Integer pkAccess;

    @Column(name = "FK_DEMARCHEID", length = 128, nullable = false)
    @NotBlank
    @Size(min = 1, max = 128)
    private String demarcheId;

    @Column(name = "USAGER_ID", nullable = false)
    private Integer usagerId;

    @Column(name = "DATE_CREATION", nullable = false)
    private Date dateCreation;

    @Column(name = "DATE_DERMODIF", nullable = false)
    private Date dateDerModif;

    @Column(name = "CONTENU", length = 10000, nullable = false)
    @NotBlank
    @Size(min = 1, max = 10000)
    private String contenu;

    @Column(name = "ACTIVE", nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "fkAccess", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DemandeBO> demandes;
    
    @OneToMany(mappedBy = "fkAccess", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<BrouillonBO> brouillons;

    public Integer getPkAccess() {
        return pkAccess;
    }

    public void setPkAccess(Integer pkAccess) {
        this.pkAccess = pkAccess;
    }

    public String getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(String demarcheId) {
        this.demarcheId = demarcheId;
    }

    public Integer getUsagerId() {
        return usagerId;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Date getDateDerModif() {
        return dateDerModif;
    }

    public String getContenu() {
        return contenu;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setUsagerId(Integer usagerId) {
        this.usagerId = usagerId;
    }

    public void setDateDerModif(Date dateDerModif) {
        this.dateDerModif = dateDerModif;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Set<DemandeBO> getDemandes() {
        return demandes;
    }

    public void setDemandes(Set<DemandeBO> demandes) {
        this.demandes = demandes;
    }

    public Set<BrouillonBO> getBrouillons() {
		return brouillons;
	}

	public void setBrouillons(Set<BrouillonBO> brouillons) {
		this.brouillons = brouillons;
	}

	public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}

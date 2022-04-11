package mc.gouv.xaf.back.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 * 
 * Classe BO de la table DEM.STATISTQUES.TYPES
 *
 * @author xdecool
 *
 */
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
	
	public Integer getPkStatistiquesTypes() {
		return pkStatistiquesTypes;
	}

	public void setPkStatistiquesTypes(Integer pkStatistiquesTypes) {
		this.pkStatistiquesTypes = pkStatistiquesTypes;
	}

	public String getIdentifiantDemande() {
		return identifiantDemande;
	}

	public void setIdentifiantDemande(String identifiantDemande) {
		this.identifiantDemande = identifiantDemande;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}

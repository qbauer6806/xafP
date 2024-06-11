package mc.gouv.xaf.apiclient2tiers.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * Bloc "demandeRecap" de certains messages envoyés au Guichet Unique
 * 
 * @author qdeme
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemandeRecapDTO {

	private Integer demandeId;
	
	private String identifiant;
	
	@JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	private Date dateCreation;
	
	private String statutSimplifie;

	public Integer getDemandeId() {
		return demandeId;
	}

	public void setDemandeId(Integer demandeId) {
		this.demandeId = demandeId;
	}

	public String getIdentifiant() {
		return identifiant;
	}

	public void setIdentifiant(String identifiant) {
		this.identifiant = identifiant;
	}

	public Date getDateCreation() {
		return dateCreation;
	}

	public void setDateCreation(Date dateCreation) {
		this.dateCreation = dateCreation;
	}

	public String getStatutSimplifie() {
		return statutSimplifie;
	}

	public void setStatutSimplifie(String statutSimplifie) {
		this.statutSimplifie = statutSimplifie;
	}
	
}

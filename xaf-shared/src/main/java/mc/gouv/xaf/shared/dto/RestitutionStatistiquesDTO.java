package mc.gouv.xaf.shared.dto;

import java.util.Date;

import javax.persistence.Column;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Modélise une stats de restitution des données 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestitutionStatistiquesDTO {

    private Integer pkStatistique;

    private String usagerId;

    private Integer httpCode;
    
    private String message;

    private Date date;
    
    private String nom;
    
    private String prenoms;
    
    private String dateNaissance;
    
    private String heureNaissance;
    
    private String villeNaissance;
    
    private String paysNaissance;

    public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getPrenoms() {
		return prenoms;
	}

	public void setPrenoms(String prenoms) {
		this.prenoms = prenoms;
	}

	public String getDateNaissance() {
		return dateNaissance;
	}

	public void setDateNaissance(String dateNaissance) {
		this.dateNaissance = dateNaissance;
	}

	public String getHeureNaissance() {
		return heureNaissance;
	}

	public void setHeureNaissance(String heureNaissance) {
		this.heureNaissance = heureNaissance;
	}

	public String getVilleNaissance() {
		return villeNaissance;
	}

	public void setVilleNaissance(String villeNaissance) {
		this.villeNaissance = villeNaissance;
	}

	public String getPaysNaissance() {
		return paysNaissance;
	}

	public void setPaysNaissance(String paysNaissance) {
		this.paysNaissance = paysNaissance;
	}

	public Integer getPkStatistique() {
		return pkStatistique;
	}

	public void setPkStatistique(Integer pkStatistique) {
		this.pkStatistique = pkStatistique;
	}

	public String getUsagerId() {
		return usagerId;
	}

	public void setUsagerId(String usagerId) {
		this.usagerId = usagerId;
	}

	public Integer getHttpCode() {
		return httpCode;
	}

	public void setHttpCode(Integer httpCode) {
		this.httpCode = httpCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}

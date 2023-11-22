package mc.gouv.xaf.back.data.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 
 * Classe BO de la table DEM.RESTITUTION_STATISTIQUES
 * <br>
 * Attention ! À chaque ajout de Set<> dans ce BO, penser à mettre à jour les transformers pour toute donnée ajoutée.
 * 
 * @author xdecool
 *
 */
@Entity
@Table(name = "DEM_RESTITUTION_STATISTIQUES")
public class RestitutionStatistiquesBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_STATISTIQUE", nullable = false)
    private Integer pkStatistique;

    @Column(name = "USAGER_ID", nullable = false)
    private String usagerId;
    
    @Column(name = "HTTP_CODE", nullable = false)
    private Integer httpCode;
    
    @Column(name = "MESSAGE", nullable = true)
    private String message;

    @Column(name = "TIMESTAMP_APPEL", nullable = false)
    private Date date;
    
    @Column(name = "NOM", nullable = true)
    private String nom;
    
    @Column(name = "PRENOMS", nullable = true)
    private String prenoms;
    
    @Column(name = "DATE_NAISSANCE", nullable = true)
    private String dateNaissance;
    
    @Column(name = "HEURE_NAISSANCE", nullable = true)
    private String heureNaissance;
    
    @Column(name = "VILLE_NAISSANCE", nullable = true)
    private String villeNaissance;
    
    @Column(name = "PAYS_NAISSANCE", nullable = true)
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

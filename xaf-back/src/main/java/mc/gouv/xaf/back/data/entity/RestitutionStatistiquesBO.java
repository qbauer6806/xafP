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
    private Integer usagerId;
    
    @Column(name = "HTTP_CODE", nullable = false)
    private Integer httpCode;
    
    @Column(name = "MESSAGE", nullable = true)
    private String message;

    @Column(name = "TIMESTAMP_APPEL", nullable = false)
    private Date date;
    
    public Integer getPkStatistique() {
		return pkStatistique;
	}

	public void setPkStatistique(Integer pkStatistique) {
		this.pkStatistique = pkStatistique;
	}

	public Integer getUsagerId() {
		return usagerId;
	}

	public void setUsagerId(Integer usagerId) {
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

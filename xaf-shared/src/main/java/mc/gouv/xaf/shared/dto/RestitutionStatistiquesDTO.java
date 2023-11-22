package mc.gouv.xaf.shared.dto;

import java.util.Date;

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

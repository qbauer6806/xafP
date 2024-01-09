package mc.gouv.xaf.shared.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Modélise une stats de restitution des données 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestitutionStatistiquesDTO {

    private Integer pkStatistique;

    private Integer usagerId;

    private Integer httpCode;
    
    private String message;

    private Date date;
    
    private String source;
    
    private String demarcheId;

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

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDemarcheId() {
		return demarcheId;
	}

	public void setDemarcheId(String demarcheId) {
		this.demarcheId = demarcheId;
	}

}

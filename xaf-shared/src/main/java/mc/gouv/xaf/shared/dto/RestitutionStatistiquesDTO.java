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
    
    private String dataProvider;

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

	public String getDataProvider() {
		return dataProvider;
	}

	public void setDataProvider(String dataProvider) {
		this.dataProvider = dataProvider;
	}

}

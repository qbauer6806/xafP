package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidIdTSDTO implements Serializable {

	private static final long serialVersionUID = -2237650305268115825L;
	private String idTS;
	
	public String getIdTS() {
		return idTS;
	}
	public void setIdTS(String idTS) {
		this.idTS = idTS;
	}

}

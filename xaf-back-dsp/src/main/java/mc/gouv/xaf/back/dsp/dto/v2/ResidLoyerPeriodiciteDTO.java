package mc.gouv.xaf.back.dsp.dto.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import mc.gouv.xaf.back.dsp.enums.ResidLoyerPeriodiciteEnum;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidLoyerPeriodiciteDTO implements Serializable {
	
	private ResidLoyerPeriodiciteEnum loyerEnum;

	private String autre;

	public ResidLoyerPeriodiciteEnum getLoyerEnum() {
		return loyerEnum;
	}

	public void setLoyerEnum(ResidLoyerPeriodiciteEnum loyerEnum) {
		this.loyerEnum = loyerEnum;
	}

	public String getAutre() {
		return autre;
	}

	public void setAutre(String autre) {
		this.autre = autre;
	}

	@Override
	public String toString() {
		return "ResidLoyerPeriodiciteDTO{" + "loyerEnum='" + loyerEnum + '\'' + ", autre='" + autre + '}';
	}	

}

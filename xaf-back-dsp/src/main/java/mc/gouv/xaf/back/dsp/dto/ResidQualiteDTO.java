package mc.gouv.xaf.back.dsp.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import mc.gouv.xaf.back.dsp.enums.common.ResidQualiteEnum;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidQualiteDTO implements Serializable {

	private static final long serialVersionUID = 8728005393250425705L;

	private ResidQualiteEnum qualiteEnum;

	private String autre;

	public ResidQualiteEnum getQualiteEnum() {
		return qualiteEnum;
	}

	public void setQualiteEnum(ResidQualiteEnum qualiteEnum) {
		this.qualiteEnum = qualiteEnum;
	}

	public String getAutre() {
		return autre;
	}

	public void setAutre(String autre) {
		this.autre = autre;
	}

	@Override
	public String toString() {
		return "ResidQualiteDTO{" + "qualiteEnum='" + qualiteEnum + '\'' + ", autre='" + autre + '}';
	}
}

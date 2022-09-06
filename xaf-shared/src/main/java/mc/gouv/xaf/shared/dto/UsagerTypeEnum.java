package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UsagerTypeEnum {

	@JsonProperty("individual")
	INDIVIDUAL,
	
	@JsonProperty("company")
	COMPANY;
	
}

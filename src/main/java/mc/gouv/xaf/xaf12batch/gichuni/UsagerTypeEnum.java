package mc.gouv.xaf.xaf12batch.gichuni;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UsagerTypeEnum {

	@JsonProperty("individual")
	INDIVIDUAL("individual"),
	
	@JsonProperty("company")
	COMPANY("company"),
	
	@JsonProperty("")
	UNDEFINED("");
	
    private String value;

    UsagerTypeEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
	
}

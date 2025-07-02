package mc.gouv.xaf.back.service.itg.sms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO représentant un paramètre d'un SMS
 *
 * @author qdeme
 */
public class SmsParamDTO {
	
    @NotNull
    @Size(min = 1, max = 50)
    @Valid
    private String key;
    
    @Size(min = 0, max = 300)
    @Valid
    private String value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "SmsParamDTO [key=" + key + ", value=" + value + "]";
	}
	
}

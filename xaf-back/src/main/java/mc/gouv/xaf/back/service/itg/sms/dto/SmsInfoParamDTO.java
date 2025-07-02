package mc.gouv.xaf.back.service.itg.sms.dto;

/**
 * DTO représentant un paramètre d'un SMS
 *
 * @author qdeme
 */
public class SmsInfoParamDTO {
	
    private String key;

    private String value;

	public SmsInfoParamDTO(String key, String value) {
		this.key = key;
		this.value = value;
	}

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

}

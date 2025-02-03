package mc.gouv.xaf.back.service.itg.sms;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO représentant un SMS
 *
 * @author qdeme
 */
public class SmsDTO {
	
    @NotNull
    @Size(min = 1, max = 40)
    @Valid
	private String identifiant;
	
    @NotNull
    @Size(min = 1, max = 20)
    @Valid
	private String statusLibel;
	
    @NotNull
    @Size(min = 1, max = 450)
    @Valid
	private String text;
	
    @Size(min = 0, max = 11)
    @Valid
	private String sender;
	
    @NotNull
    @Size(min = 1, max = 20) // TODO 20 ?
    @Valid
	private List<String> to;
	
	private List<SmsParamDTO> params;

	public String getIdentifiant() {
		return identifiant;
	}

	public void setIdentifiant(String identifiant) {
		this.identifiant = identifiant;
	}

	public String getStatusLibel() {
		return statusLibel;
	}

	public void setStatusLibel(String statusLibel) {
		this.statusLibel = statusLibel;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public List<String> getTo() {
		return to;
	}

	public void setTo(List<String> to) {
		this.to = to;
	}

	public List<SmsParamDTO> getParams() {
		return params;
	}

	public void setParams(List<SmsParamDTO> params) {
		this.params = params;
	}

	@Override
	public String toString() {
		return "SmsDTO [identifiant=" + identifiant + ", statusLibel=" + statusLibel + ", text=" + text + ", sender="
				+ sender + ", to=" + to + ", params=" + params + "]";
	}

}

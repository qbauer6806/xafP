package mc.gouv.xaf.back.dsp.dto.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidContactsDTO implements Serializable {

	private static final long serialVersionUID = 7508929356926248233L;

	private String email;

	private String telephone1Prefix;

	private String telephone1;

	private String typeCommunication;

	private String langue;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelephone1Prefix() {
		return telephone1Prefix;
	}

	public void setTelephone1Prefix(String telephone1Prefix) {
		this.telephone1Prefix = telephone1Prefix;
	}

	public String getTelephone1() {
		return telephone1;
	}

	public void setTelephone1(String telephone1) {
		this.telephone1 = telephone1;
	}

	public String getTypeCommunication() {
		return typeCommunication;
	}

	public void setTypeCommunication(String typeCommunication) {
		this.typeCommunication = typeCommunication;
	}

	public String getLangue() {
		return langue;
	}

	public void setLangue(String langue) {
		this.langue = langue;
	}

	@Override
	public String toString() {
		return "ResidContactsDTO{" + "email='" + email + '\'' + ", telephone1Prefix='" + telephone1Prefix + '\''
				+ ", telephone1='" + telephone1 + '\'' + ", typeCommunication='" + typeCommunication + '\''
				+ ", langue='" + langue + '}';
	}

}

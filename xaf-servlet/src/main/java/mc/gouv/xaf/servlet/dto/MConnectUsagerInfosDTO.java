package mc.gouv.xaf.servlet.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MConnectUsagerInfosDTO {

	@JsonProperty("given_name")
	private String givenName;
	
	@JsonProperty("family_name")
	private String familyName;
	
	@JsonProperty("birth_name")
	private String birthName;
	
	private String gender;
	
	@JsonProperty("birth_place")
	private String birthPlace;
	
	@JsonProperty("birth_datetime")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMddHHmmss")
	private Date birthDatetime;
	
	private String authority;
	
	@JsonProperty("birth_place_country")
	private String birthPlaceCountry;
	
	@JsonProperty("birth_place_city")
	private String birthPlaceCity;

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public String getBirthName() {
		return birthName;
	}

	public void setBirthName(String birthName) {
		this.birthName = birthName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getBirthPlace() {
		return birthPlace;
	}

	public void setBirthPlace(String birthPlace) {
		this.birthPlace = birthPlace;
	}

	public Date getBirthDatetime() {
		return birthDatetime;
	}

	public void setBirthDatetime(Date birthDatetime) {
		this.birthDatetime = birthDatetime;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public String getBirthPlaceCountry() {
		return birthPlaceCountry;
	}

	public void setBirthPlaceCountry(String birthPlaceCountry) {
		this.birthPlaceCountry = birthPlaceCountry;
	}

	public String getBirthPlaceCity() {
		return birthPlaceCity;
	}

	public void setBirthPlaceCity(String birthPlaceCity) {
		this.birthPlaceCity = birthPlaceCity;
	}

	@Override
	public String toString() {
		return "MConnectUsagerInfosDTO [givenName=" + givenName + ", familyName=" + familyName + ", birthName="
				+ birthName + ", gender=" + gender + ", birthPlace=" + birthPlace + ", birthDatetime=" + birthDatetime
				+ ", authority=" + authority + ", birthPlaceCountry=" + birthPlaceCountry + ", birthPlaceCity="
				+ birthPlaceCity + "]";
	}
	
}

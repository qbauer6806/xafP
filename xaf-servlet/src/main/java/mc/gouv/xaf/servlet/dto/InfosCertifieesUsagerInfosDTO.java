package mc.gouv.xaf.servlet.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InfosCertifieesUsagerInfosDTO {

	@JsonProperty("given_name")
	private String prenom;
	
	@JsonProperty("family_name")
	private String nom;
	
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
	
	// Champ calculé par xaf-servlet à partir de "gender"
	private Short titre;

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
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
		if ("M".equals(gender)) {
			titre = 0;
		}
		else if ("F".equals(gender)) {
			titre = 1;
		}
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

	public Short getTitre() {
		return titre;
	}

	public void setTitre(Short titre) {
		this.titre = titre;
	}

	@Override
	public String toString() {
		return "MConnectUsagerInfosDTO [givenName=" + prenom + ", familyName=" + nom + ", birthName="
				+ birthName + ", gender=" + gender + ", birthPlace=" + birthPlace + ", birthDatetime=" + birthDatetime
				+ ", authority=" + authority + ", birthPlaceCountry=" + birthPlaceCountry + ", birthPlaceCity="
				+ birthPlaceCity + "]";
	}
	
}

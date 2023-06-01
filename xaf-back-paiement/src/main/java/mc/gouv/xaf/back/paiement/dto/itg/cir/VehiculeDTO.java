package mc.gouv.xaf.back.paiement.dto.itg.cir;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VehiculeDTO {

	private String numImmat;

    private String nomPropr;

    private String prenomPropr;

    private String adresse1;

    private String adresse2;

    private String nationalite;

    private String lieuNaissance;

    private String dateNaissance;
    
    private Integer registre;

	public String getNumImmat() {
		return numImmat;
	}

	public void setNumImmat(String numImmat) {
		this.numImmat = numImmat;
	}

	public String getNomPropr() {
		return nomPropr;
	}

	public void setNomPropr(String nomPropr) {
		this.nomPropr = nomPropr;
	}

	public String getPrenomPropr() {
		return prenomPropr;
	}

	public void setPrenomPropr(String prenomPropr) {
		this.prenomPropr = prenomPropr;
	}

	public String getAdresse1() {
		return adresse1;
	}

	public void setAdresse1(String adresse1) {
		this.adresse1 = adresse1;
	}

	public String getAdresse2() {
		return adresse2;
	}

	public void setAdresse2(String adresse2) {
		this.adresse2 = adresse2;
	}

	public String getNationalite() {
		return nationalite;
	}

	public void setNationalite(String nationalite) {
		this.nationalite = nationalite;
	}

	public String getLieuNaissance() {
		return lieuNaissance;
	}

	public void setLieuNaissance(String lieuNaissance) {
		this.lieuNaissance = lieuNaissance;
	}

	public String getDateNaissance() {
		return dateNaissance;
	}

	public void setDateNaissance(String dateNaissance) {
		this.dateNaissance = dateNaissance;
	}

	public Integer getRegistre() {
		return registre;
	}

	public void setRegistre(Integer registre) {
		this.registre = registre;
	}

}

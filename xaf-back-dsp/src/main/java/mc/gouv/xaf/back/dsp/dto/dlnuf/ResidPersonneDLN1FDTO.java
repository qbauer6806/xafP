package mc.gouv.xaf.back.dsp.dto.dlnuf;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import mc.gouv.xaf.back.dsp.enums.common.ResidCiviliteEnum;
import mc.gouv.xaf.back.dsp.enums.v2.ResidRelationDLN1FEnum;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidPersonneDLN1FDTO implements Serializable {

	private static final long serialVersionUID = 2524864271822373336L;
	
	private ResidRelationDLN1FEnum relationPersonne;
	
	private ResidCiviliteEnum titrePersonne;

	private String nomPersonne;

	private String prenomPersonne;

	private String dateNaissancePersonne;

	private String nationalitePersonne;

	private String lieuScolariteTravail;

	public ResidRelationDLN1FEnum getRelationPersonne() {
		return relationPersonne;
	}

	public void setRelationPersonne(ResidRelationDLN1FEnum relationPersonne) {
		this.relationPersonne = relationPersonne;
	}

	public ResidCiviliteEnum getTitrePersonne() {
		return titrePersonne;
	}

	public void setTitrePersonne(ResidCiviliteEnum titrePersonne) {
		this.titrePersonne = titrePersonne;
	}

	public String getNomPersonne() {
		return nomPersonne;
	}

	public void setNomPersonne(String nomPersonne) {
		this.nomPersonne = nomPersonne;
	}

	public String getPrenomPersonne() {
		return prenomPersonne;
	}

	public void setPrenomPersonne(String prenomPersonne) {
		this.prenomPersonne = prenomPersonne;
	}

	public String getDateNaissancePersonne() {
		return dateNaissancePersonne;
	}

	public void setDateNaissancePersonne(String dateNaissancePersonne) {
		this.dateNaissancePersonne = dateNaissancePersonne;
	}

	public String getNationalitePersonne() {
		return nationalitePersonne;
	}

	public void setNationalitePersonne(String nationalitePersonne) {
		this.nationalitePersonne = nationalitePersonne;
	}

	public String getLieuScolariteTravail() {
		return lieuScolariteTravail;
	}

	public void setLieuScolariteTravail(String lieuScolariteTravail) {
		this.lieuScolariteTravail = lieuScolariteTravail;
	}
	
	@Override
	public String toString() {
		return "ResidPersonneDTO{" + "relationPersonne='" + relationPersonne + '\'' + ", titrePersonne='" + titrePersonne + '\''
				+ ", nomPersonne='" + nomPersonne + '\'' + ", prenomPersonne='" + prenomPersonne + '\''
				+ ", dateNaissancePersonne='" + dateNaissancePersonne + '\'' + ", nationalitePersonne='"
				+ nationalitePersonne + '\'' + ", lieuScolariteTravail='" + lieuScolariteTravail + '}';
	}
	
}

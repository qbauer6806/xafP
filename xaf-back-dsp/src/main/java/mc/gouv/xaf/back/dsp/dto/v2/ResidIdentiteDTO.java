package mc.gouv.xaf.back.dsp.dto.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import mc.gouv.xaf.back.dsp.enums.ResidCiviliteEnum;
import mc.gouv.xaf.back.dsp.enums.ResidSexeEnum;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidIdentiteDTO implements Serializable {

	private static final long serialVersionUID = 2762334113370075251L;

	private ResidCiviliteEnum titreUsager;

	private String nomUsager;

	private String nomUsageUsager;

	private String prenomUsager;

	private String dateNaissanceUsager;

	private String heureNaissanceUsager;

	private String villeNaissanceUsager;

	private String paysNaissanceUsager;

	private ResidSexeEnum sexeUsager;

	private boolean personnaliteSensible;

	public ResidCiviliteEnum getTitreUsager() {
		return titreUsager;
	}

	public void setTitreUsager(ResidCiviliteEnum titreUsager) {
		this.titreUsager = titreUsager;
	}

	public String getNomUsager() {
		return nomUsager;
	}

	public void setNomUsager(String nomUsager) {
		this.nomUsager = nomUsager;
	}

	public String getNomUsageUsager() {
		return nomUsageUsager;
	}

	public void setNomUsageUsager(String nomUsageUsager) {
		this.nomUsageUsager = nomUsageUsager;
	}

	public String getPrenomUsager() {
		return prenomUsager;
	}

	public void setPrenomUsager(String prenomUsager) {
		this.prenomUsager = prenomUsager;
	}

	public String getVilleNaissanceUsager() {
		return villeNaissanceUsager;
	}

	public void setVilleNaissanceUsager(String villeNaissanceUsager) {
		this.villeNaissanceUsager = villeNaissanceUsager;
	}

	public String getPaysNaissanceUsager() {
		return paysNaissanceUsager;
	}

	public void setPaysNaissanceUsager(String paysNaissanceUsager) {
		this.paysNaissanceUsager = paysNaissanceUsager;
	}

	public ResidSexeEnum getSexeUsager() {
		return sexeUsager;
	}

	public void setSexeUsager(ResidSexeEnum sexeUsager) {
		this.sexeUsager = sexeUsager;
	}

	public boolean isPersonnaliteSensible() {
		return personnaliteSensible;
	}

	public void setPersonnaliteSensible(boolean personnaliteSensible) {
		this.personnaliteSensible = personnaliteSensible;
	}

	public String getDateNaissanceUsager() {
		return dateNaissanceUsager;
	}

	public void setDateNaissanceUsager(String dateNaissanceUsager) {
		this.dateNaissanceUsager = dateNaissanceUsager;
	}

	public String getHeureNaissanceUsager() {
		return heureNaissanceUsager;
	}

	public void setHeureNaissanceUsager(String heureNaissanceUsager) {
		this.heureNaissanceUsager = heureNaissanceUsager;
	}

	@Override
	public String toString() {
		return "ResidIdentiteDTO{" + "titreUsager='" + titreUsager + '\'' + ", nomUsager='" + nomUsager + '\''
				+ ", nomUsageUsager='" + nomUsageUsager + '\'' + ", prenomUsager='" + prenomUsager + '\''
				+ ", dateNaissanceUsager='" + dateNaissanceUsager + '\'' + ", heureNaissanceUsager='"
				+ heureNaissanceUsager + '\'' + ", villeNaissanceUsager='" + villeNaissanceUsager + '\''
				+ ", paysNaissanceUsager='" + paysNaissanceUsager + '\'' + ", sexeUsager=" + sexeUsager + '\''
				+ ", personnaliteSensible=" + personnaliteSensible + '}';
	}
}

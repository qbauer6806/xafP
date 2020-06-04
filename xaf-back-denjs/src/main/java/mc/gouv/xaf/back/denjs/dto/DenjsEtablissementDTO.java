package mc.gouv.xaf.back.denjs.dto;

/**
 * DTO représentant un établissement scolaire
 * 
 * @author qdeme
 *
 */
public class DenjsEtablissementDTO {
	
	private String code;
	
	private String nom;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

}

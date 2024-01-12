package mc.gouv.candifp.frontserver.movetoxaf.dto;

/**
 * 
 * Classe permettant de matérialiser une erreur en retour de l'API TGF pour les IBAN
 * 
 * @author qdeme
 * 
 */
public class TgfApiIbanResponseErreurDTO {
	
	private String code;
	
	private String champ;
	
	private String message;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getChamp() {
		return champ;
	}

	public void setChamp(String champ) {
		this.champ = champ;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}

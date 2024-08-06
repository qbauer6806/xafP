package mc.gouv.xaf.front.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Classe permettant de matérialiser une erreur en retour de l'API TGF pour les IBAN
 * 
 * @author qdeme
 * 
 */
@Setter
@Getter
public class TgfApiIbanResponseErreurDTO {
	
	private String code;
	
	private String champ;
	
	private String message;

}

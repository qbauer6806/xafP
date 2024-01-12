package mc.gouv.candifp.frontserver.movetoxaf.dto;

/**
 * 
 * Classe permettant de matérialiser une erreur en retour de l'API TGF pour les IBAN
 * 
 * @author qdeme
 * 
 */
public class TgfApiIbanResponseDTO {
	
	private TgfApiIbanResponseErreurDTO[] erreurs;

	public TgfApiIbanResponseErreurDTO[] getErreurs() {
		return erreurs;
	}

	public void setErreurs(TgfApiIbanResponseErreurDTO[] erreurs) {
		this.erreurs = erreurs;
	}

}

package mc.gouv.af.servlet.dto;

public class TgfApiIbanResponseDTO {
	
	private TgfApiIbanResponseErreurDTO[] erreurs;

	public TgfApiIbanResponseErreurDTO[] getErreurs() {
		return erreurs;
	}

	public void setErreurs(TgfApiIbanResponseErreurDTO[] erreurs) {
		this.erreurs = erreurs;
	}

}

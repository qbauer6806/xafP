package mc.gouv.xaf.backweb.formbean;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Formulaire pour le preview des emails
 * 
 * @author mboutelier.ext
 *
 */
public class MailPreviewFormBean {

    @NotEmpty
	private String action;

	private String codeMotifChoisi;

	@NotNull
	private Integer pkDemande;

	private String commentaire;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getCodeMotifChoisi() {
		return codeMotifChoisi;
	}

	public void setCodeMotifChoisi(String codeMotifChoisi) {
		this.codeMotifChoisi = codeMotifChoisi;
	}

	public Integer getPkDemande() {
		return pkDemande;
	}

	public void setPkDemande(Integer pkDemande) {
		this.pkDemande = pkDemande;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

}

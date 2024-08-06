package mc.gouv.xaf.backweb.formbean;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulaire pour le preview des emails
 * 
 * @author mboutelier.ext
 *
 */
@Setter
@Getter
public class PreviewFormBean {

	@NotNull
	private String action;

	private String codeMotifChoisi;

	@NotNull
	private Integer pkDemande;

	private String commentaire;

}

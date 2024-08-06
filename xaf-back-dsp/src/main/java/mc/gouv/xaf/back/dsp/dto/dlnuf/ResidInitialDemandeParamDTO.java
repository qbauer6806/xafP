package mc.gouv.xaf.back.dsp.dto.dlnuf;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ResidInitialDemandeParamDTO implements Serializable {

    private static final long serialVersionUID = 1151801717747924423L;

	private String nom;
	private String nomusage;
	private String prenom;
	private String dateNaissance;
	private String villeNaissance;
	private String paysNaissance;
	private String email;
	private String titre;

}

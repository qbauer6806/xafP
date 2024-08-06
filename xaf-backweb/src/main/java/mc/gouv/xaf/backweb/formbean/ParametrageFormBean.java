package mc.gouv.xaf.backweb.formbean;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ParametrageFormBean {
	
	private String nomDemarche;
	
    private String emailService;

    private String emailReplyto;
    
    private String emailReplytoNom;
    
    private String emailFrom;
    
    private String emailFromNom;
    private String identifiantPrefixe;

	private String nomDirection;

	private String nomSousDirection;

	private String nomFooter;

	private String adresseService;
    
	private String nomSousDirectionComplement;
	private String telephoneService;
	private String nomDemarcheEn;
	private String nomDirectionEn;
	private String nomSousDirectionEn;
	private String nomSousDirectionComplementEn;

}

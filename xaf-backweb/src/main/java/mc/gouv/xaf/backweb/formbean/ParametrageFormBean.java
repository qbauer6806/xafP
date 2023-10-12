package mc.gouv.xaf.backweb.formbean;

public class ParametrageFormBean {
	
	private String nomDemarche;
	
    private String emailService;
    
    private String emailServiceNom;
    
    private String emailReplyto;
    
    private String emailReplytoNom;
    
    private String emailFrom;
    
    private String emailFromNom;
    
    private String identifiantPrefixe;
    
    private boolean langueFr;
    
    private boolean langueEn;
    
    private boolean langueIt;

	public String getNomDemarche() {
		return nomDemarche;
	}

	public void setNomDemarche(String nomDemarche) {
		this.nomDemarche = nomDemarche;
	}

	public String getEmailService() {
		return emailService;
	}

	public void setEmailService(String emailService) {
		this.emailService = emailService;
	}

	public String getEmailServiceNom() {
		return emailServiceNom;
	}

	public void setEmailServiceNom(String emailServiceNom) {
		this.emailServiceNom = emailServiceNom;
	}

	public String getEmailReplyto() {
		return emailReplyto;
	}

	public void setEmailReplyto(String emailReplyto) {
		this.emailReplyto = emailReplyto;
	}

	public String getEmailReplytoNom() {
		return emailReplytoNom;
	}

	public void setEmailReplytoNom(String emailReplytoNom) {
		this.emailReplytoNom = emailReplytoNom;
	}

	public String getEmailFrom() {
		return emailFrom;
	}

	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}

	public String getEmailFromNom() {
		return emailFromNom;
	}

	public void setEmailFromNom(String emailFromNom) {
		this.emailFromNom = emailFromNom;
	}

	public String getIdentifiantPrefixe() {
		return identifiantPrefixe;
	}

	public void setIdentifiantPrefixe(String identifiantPrefixe) {
		this.identifiantPrefixe = identifiantPrefixe;
	}

	public boolean getLangueFr() {
		return langueFr;
	}

	public void setLangueFr(boolean langueFr) {
		this.langueFr = langueFr;
	}

	public boolean getLangueEn() {
		return langueEn;
	}

	public void setLangueEn(boolean langueEn) {
		this.langueEn = langueEn;
	}

	public boolean getLangueIt() {
		return langueIt;
	}

	public void setLangueIt(boolean langueIt) {
		this.langueIt = langueIt;
	}
	
}

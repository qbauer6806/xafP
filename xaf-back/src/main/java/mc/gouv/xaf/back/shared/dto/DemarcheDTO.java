package mc.gouv.xaf.back.shared.dto;

/**
 * Modélise le contenu d'une démarche
 * 
 * @author qdeme
 *
 */
public class DemarcheDTO {

    private String pkDemarches;
    
    private String nom;

    private String emailService;
    
    private String emailServiceNom;
    
    private String emailReplyto;
    
    private String emailReplytoNom;
    
    private String emailFrom;
    
    private String emailFromNom;
    
    private String identifiantPrefixe;

    public String getPkDemarches() {
        return pkDemarches;
    }

    public void setPkDemarches(String pkDemarches) {
        this.pkDemarches = pkDemarches;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
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
    
}

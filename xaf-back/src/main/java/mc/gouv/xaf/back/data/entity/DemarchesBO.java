package mc.gouv.xaf.back.data.entity;

import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * 
 * Classe BO de la table DEM.DEMARCHES
 * 
 * @author qdeme
 *
 */
@Entity
@Table(name = "DEM_DEMARCHES")
public class DemarchesBO {

    @Id
    @NotBlank
    @Column(name = "PK_DEMARCHEID", nullable = false)
    @Size(min = 1, max = 128)
    private String pkDemarches;
    
    @Column(name = "NOM", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String nom;
    
    @Column(name = "NOM_EN", length = 256)
    @Size(min = 0, max = 256)
    private String nomEn;

    @Column(name = "EMAIL_SERVICE", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String emailService;

    @Column(name = "EMAIL_REPLYTO", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String emailReplyto;

    @Column(name = "EMAIL_REPLYTO_NOM", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String emailReplytoNom;

    @Column(name = "EMAIL_FROM", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String emailFrom;

    @Column(name = "EMAIL_FROM_NOM", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String emailFromNom;

    @Column(name = "NOM_DIRECTION", length = 256)
    @NotBlank
    @Size(min = 1, max = 256)
    private String nomDirection;

    @Column(name = "NOM_SOUS_DIRECTION", length = 256)
    @Size(max = 256)
    private String nomSousDirection;

    @Column(name = "NOM_FOOTER", length = 256)
    @NotBlank
    @Size(min = 1, max = 256)
    private String nomFooter;

    @Column(name = "ADRESSE_SERVICE", length = 256)
    @NotBlank
    @Size(min = 1, max = 256)
    private String adresseService;
    
    @Column(name = "IDENTIFIANT_PREFIXE", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 128)
    private String identifiantPrefixe;
    
    @OneToMany(mappedBy = "demarche", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PeriodesOuvertureBO> periodesOuverture;

    @OneToMany(mappedBy = "demarche", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PropertiesBO> properties;

    @Column(name = "LANGUES", length = 256)
    @Size(max = 256)
    private String langues;

    @Column(name = "NOM_SOUS_DIRECTION_COMPLEMENT", length = 256)
    @Size(max = 256)
    private String nomSousDirectionComplement;
    @Column(name = "TELEPHONE_SERVICE", length = 256)
    @Size(max = 256)
    private String telephoneService;

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

	public Set<PeriodesOuvertureBO> getPeriodesOuverture() {
		return periodesOuverture;
	}

	public void setPeriodesOuverture(Set<PeriodesOuvertureBO> periodesOuverture) {
		this.periodesOuverture = periodesOuverture;
	}

    public Set<PropertiesBO> getProperties() {
        return properties;
    }

    public void setProperties(Set<PropertiesBO> properties) {
        this.properties = properties;
    }

	public String getLangues() {
		return langues;
	}

	public void setLangues(String langues) {
		this.langues = langues;
	}

	public String getNomEn() {
		return nomEn;
	}

	public void setNomEn(String nomEn) {
		this.nomEn = nomEn;
	}

    public String getNomDirection() {
        return nomDirection;
    }

    public void setNomDirection(String nomDirection) {
        this.nomDirection = nomDirection;
    }

    public String getNomSousDirection() {
        return nomSousDirection;
    }

    public void setNomSousDirection(String nomSousDirection) {
        this.nomSousDirection = nomSousDirection;
    }

    public String getNomFooter() {
        return nomFooter;
    }

    public void setNomFooter(String nomFooter) {
        this.nomFooter = nomFooter;
    }

    public String getAdresseService() {
        return adresseService;
    }

    public void setAdresseService(String adresseService) {
        this.adresseService = adresseService;
    }

    public String getNomSousDirectionComplement() {
        return nomSousDirectionComplement;
    }

    public void setNomSousDirectionComplement(String nomSousDirectionComplement) {
        this.nomSousDirectionComplement = nomSousDirectionComplement;
    }

    public String getTelephoneService() {
        return telephoneService;
    }

    public void setTelephoneService(String telephoneService) {
        this.telephoneService = telephoneService;
    }
}

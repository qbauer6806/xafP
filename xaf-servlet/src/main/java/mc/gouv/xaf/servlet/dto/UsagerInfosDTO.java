package mc.gouv.xaf.servlet.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * Usager loggé géré par l'application de gestion des usagers (login).
 * </p>
 * <p>
 * Cet usager est partagé avec les autres téléservices via un service rest (voir
 * mc.gouv.servicerest.usager.model.UsagerBean).
 * </p>
 * <p>
 * Le nom historique du package mc.gouv.tp.crosscontext est conservé pour ne pas impacter le code existant (l'usager fut
 * un temps partagé entre les téléservices via un contexte partagé).
 * </p>
 * 
 * @author dinfo10
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsagerInfosDTO implements Serializable {

    private static final long serialVersionUID = -7219376931453637516L;

    // gender identifiers and labels
    public static final short GENDER_MR_INDEX = 0;
    public static final short GENDER_MME_INDEX = 1;
    public static final short GENDER_MLLE_INDEX = 2;

    protected Integer id; // technical internal identifier
    protected String login;
    protected Short etat;
    protected String email;
    protected Date dateCreation;
    protected Date dateActivation;
    protected Date dateDerConnexion;
    protected Short titre;
    protected String prenom;
    protected String nom;
    protected String raisonSociale;
    protected String adresse1;
    protected String adresse2;
    protected String complementAdresse;
    protected String codePostal;
    protected String ville;
    // Le jour ou il y aura de l'internationalisation, il faudra retourner
    // le code pays plutot que le pays et y associer le nom du pays dans un
    // fichier de resources localisé.
    // Mais pour l'instant on retourne directement le nom de pays tel qu'en BD
    // pour simplifier les choses.
    protected String nomPays;

    protected String paysId;

    protected String paysCode;

    protected boolean isUsagerCourrier = false;
    
    protected Integer accessId;
    
    protected KeycloakTokenInfo tokenInfo;
    
    protected InfosCertifieesUsagerInfosDTO infosCertifiees;
    
    protected boolean mConnect = false;

    public String getTitreLabel() {
        if (titre == null) {
            return null;
        }
        switch (titre) {
            case GENDER_MR_INDEX:
                return "Monsieur";
            case GENDER_MME_INDEX:
                return "Madame";
            case GENDER_MLLE_INDEX:
                return "Mademoiselle";
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "UsagerReadOnlyBean [id=" + id + ", login=" + login + "]";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Short getEtat() {
        return etat;
    }

    public void setEtat(Short etat) {
        this.etat = etat;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Date getDateActivation() {
        return dateActivation;
    }

    public void setDateActivation(Date dateActivation) {
        this.dateActivation = dateActivation;
    }

    public Date getDateDerConnexion() {
        return dateDerConnexion;
    }

    public void setDateDerConnexion(Date dateDerConnexion) {
        this.dateDerConnexion = dateDerConnexion;
    }

    public Short getTitre() {
        return titre;
    }

    public void setTitre(Short titre) {
        this.titre = titre;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public String getAdresse1() {
        return adresse1;
    }

    public void setAdresse1(String adresse1) {
        this.adresse1 = adresse1;
    }

    public String getAdresse2() {
        return adresse2;
    }

    public void setAdresse2(String adresse2) {
        this.adresse2 = adresse2;
    }

    public String getComplementAdresse() {
        return complementAdresse;
    }

    public void setComplementAdresse(String complementAdresse) {
        this.complementAdresse = complementAdresse;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getNomPays() {
        return nomPays;
    }

    public void setNomPays(String nomPays) {
        this.nomPays = nomPays;
    }

    public String getPaysId() {
        return paysId;
    }

    public void setPaysId(String paysId) {
        this.paysId = paysId;
    }

    public String getPaysCode() {
        return paysCode;
    }

    public void setPaysCode(String paysCode) {
        this.paysCode = paysCode;
    }

    @JsonProperty("isUsagerCourrier")
    public boolean isUsagerCourrier() {
        return isUsagerCourrier;
    }

    @JsonProperty("isUsagerCourrier")
    public void setUsagerCourrier(boolean isUsagerCourrier) {
        this.isUsagerCourrier = isUsagerCourrier;
    }

	public Integer getAccessId() {
		return accessId;
	}

	public void setAccessId(Integer accessId) {
		this.accessId = accessId;
	}

	public KeycloakTokenInfo getTokenInfo() {
		return tokenInfo;
	}

	public void setTokenInfo(KeycloakTokenInfo tokenInfo) {
		this.tokenInfo = tokenInfo;
	}

	public InfosCertifieesUsagerInfosDTO getInfosCertifiees() {
		return infosCertifiees;
	}

	public void setInfosCertifiees(InfosCertifieesUsagerInfosDTO infosCertifiees) {
		this.infosCertifiees = infosCertifiees;
	}

	public boolean ismConnect() {
		return mConnect;
	}

	public void setmConnect(boolean mConnect) {
		this.mConnect = mConnect;
	}

}

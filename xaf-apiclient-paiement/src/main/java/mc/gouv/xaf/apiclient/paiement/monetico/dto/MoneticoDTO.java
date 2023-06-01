package mc.gouv.xaf.apiclient.paiement.monetico.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Modélise une donnée de paiement pour l'interface Monetico
 * Pour plus d'informations se reporter à la doc technique de Monetico
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoneticoDTO {

    /**
     * ex: "1234567"
     */
    private String tpe;

    /**
     * Version du système de paiement utilisée
     */
    private String version;

    /**
     * Date de la commande au format JJ/MM/AAAA:HH:MM:SS
     */
    private String date;

    /**
     * Montant TTC de la somme à payer
     */
    private String montant;

    /**
     * Référence unique de la commande sur 12 caractères
     */
    private String reference;

    /**
     * Code de la langue d'affichage de la page de paiement (2 caractères)
     */
    private String lgue;

    /**
     * ex: f97861e0f3e296b7eece2cfd86dc46c43ac88049
     */
    private String mac;

    /**
     * Informations relatives à la commande au format JSON en Base64
     */
    private String contexte_commande;

    /**
     * Nom de la société lié au numéro de TPE
     */
    private String societe;

    /**
     * Zone de texte libre (3200 caractères)
     */
    @JsonProperty("texte-libre")
    private String texteLibre;

    /**
     * Email du client réalisant la transaction
     */
    private String mail;

    /**
     * URL par laquelle l’acheteur revient sur le site du commerçant suite à un paiement accepté
     */
    @JsonProperty("url_retour_ok")
    private String urlRetourOk;

    /**
     * URL par laquelle l’acheteur revient sur le site du commerçant suite à un paiement échoué
     */
    @JsonProperty("url_retour_err")
    private String urlRetourErr;

    private String threeDSecureChallenge;
    private String mode_affichage;
    private String nbrech = "";
    private String dateech1 = "";
    private String montantech1 = "";
    private String dateech2 = "";
    private String montantech2 = "";
    private String dateech3 = "";
    private String montantech3 = "";
    private String dateech4 = "";
    private String montantech4 = "";
    private String libelleMonetique = "";
    private String libelleMonetiqueLocalite = "";

    public MoneticoDTO() {
        super();
    }

    @JsonGetter("TPE")
    public String getTpe() {
        return tpe;
    }

    @JsonSetter("TPE")
    public void setTpe(String tpe) {
        this.tpe = tpe;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getLgue() {
        return lgue;
    }

    public void setLgue(String lgue) {
        this.lgue = lgue;
    }

    @JsonGetter("MAC")
    public String getMac() {
        return mac;
    }

    @JsonSetter("MAC")
    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getContexte_commande() {
        return contexte_commande;
    }

    public void setContexte_commande(String contexte_commande) {
        this.contexte_commande = contexte_commande;
    }

    public String getSociete() {
        return societe;
    }

    public void setSociete(String societe) {
        this.societe = societe;
    }

    public String getTexteLibre() {
        return texteLibre;
    }

    public void setTexteLibre(String texteLibre) {
        this.texteLibre = texteLibre;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getUrlRetourOk() {
        return urlRetourOk;
    }

    public void setUrlRetourOk(String urlRetourOk) {
        this.urlRetourOk = urlRetourOk;
    }

    public String getUrlRetourErr() {
        return urlRetourErr;
    }

    public void setUrlRetourErr(String urlRetourErr) {
        this.urlRetourErr = urlRetourErr;
    }

    @JsonGetter("ThreeDSecureChallenge")
    public String getThreeDSecureChallenge() {
        return threeDSecureChallenge;
    }

    @JsonSetter("ThreeDSecureChallenge")
    public void setThreeDSecureChallenge(String threeDSecureChallenge) {
        this.threeDSecureChallenge = threeDSecureChallenge;
    }

    public String getMode_affichage() {
        return mode_affichage;
    }

    public void setMode_affichage(String mode_affichage) {
        this.mode_affichage = mode_affichage;
    }

    public String getNbrech() {
        return nbrech;
    }

    public void setNbrech(String nbrech) {
        this.nbrech = nbrech;
    }

    public String getDateech1() {
        return dateech1;
    }

    public void setDateech1(String dateech1) {
        this.dateech1 = dateech1;
    }

    public String getMontantech1() {
        return montantech1;
    }

    public void setMontantech1(String montantech1) {
        this.montantech1 = montantech1;
    }

    public String getDateech2() {
        return dateech2;
    }

    public void setDateech2(String dateech2) {
        this.dateech2 = dateech2;
    }

    public String getMontantech2() {
        return montantech2;
    }

    public void setMontantech2(String montantech2) {
        this.montantech2 = montantech2;
    }

    public String getDateech3() {
        return dateech3;
    }

    public void setDateech3(String dateech3) {
        this.dateech3 = dateech3;
    }

    public String getMontantech3() {
        return montantech3;
    }

    public void setMontantech3(String montantech3) {
        this.montantech3 = montantech3;
    }

    public String getDateech4() {
        return dateech4;
    }

    public void setDateech4(String dateech4) {
        this.dateech4 = dateech4;
    }

    public String getMontantech4() {
        return montantech4;
    }

    public void setMontantech4(String montantech4) {
        this.montantech4 = montantech4;
    }

    @Override
    public String toString() {
        return "PaiementDTO{" +
                "TPE='" + tpe + '\'' +
                ", version='" + version + '\'' +
                ", date='" + date + '\'' +
                ", montant='" + montant + '\'' +
                ", reference='" + reference + '\'' +
                ", lgue='" + lgue + '\'' +
                ", MAC='" + mac + '\'' +
                ", contexte_commande='" + contexte_commande + '\'' +
                ", societe='" + societe + '\'' +
                ", texteLibre='" + texteLibre + '\'' +
                ", mail='" + mail + '\'' +
                ", urlRetourOk='" + urlRetourOk + '\'' +
                ", urlRetourErr='" + urlRetourErr + '\'' +
                //", ThreeDSecureChallenge='" + ThreeDSecureChallenge + '\'' +
                ", mode_affichage='" + mode_affichage + '\'' +
                ", nbrech='" + nbrech + '\'' +
                ", dateech1='" + dateech1 + '\'' +
                ", montantech1='" + montantech1 + '\'' +
                ", dateech2='" + dateech2 + '\'' +
                ", montantech2='" + montantech2 + '\'' +
                ", dateech3='" + dateech3 + '\'' +
                ", montantech3='" + montantech3 + '\'' +
                ", dateech4='" + dateech4 + '\'' +
                ", montantech4='" + montantech4 + '\'' +
                ", libelleMonetique='" + libelleMonetique + '\'' +
                ", libelleMonetiqueLocalite='" + libelleMonetiqueLocalite + '\'' +
                '}';
    }


	public String getLibelleMonetique() {
		return libelleMonetique;
	}


	public void setLibelleMonetique(String libelleMonetique) {
		this.libelleMonetique = libelleMonetique;
	}


	public String getLibelleMonetiqueLocalite() {
		return libelleMonetiqueLocalite;
	}


	public void setLibelleMonetiqueLocalite(String libelleMonetiqueLocalite) {
		this.libelleMonetiqueLocalite = libelleMonetiqueLocalite;
	}
}

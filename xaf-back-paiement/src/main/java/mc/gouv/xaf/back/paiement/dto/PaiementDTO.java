package mc.gouv.xaf.back.paiement.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Modélise une donnée de paiement pour l'interface Monetico
 * Pour plus d'informaations se reporter à la doc technique de Monetico
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class PaiementDTO {

    /**
     * ex: "1234567"
     */
    private String TPE;

    /**
     * Version du système de paiement utilisée
     */
    @Setter
    @Getter
    private String version;

    /**
     * Date de la commande au format JJ/MM/AAAA:HH:MM:SS
     */
    @Setter
    @Getter
    private String date;

    /**
     * Montant TTC de la somme à payer
     */
    @Setter
    @Getter
    private String montant;

    /**
     * Référence unique de la commande sur 12 caractères
     */
    @Setter
    @Getter
    private String reference;

    /**
     * Code de la langue d'affichage de la page de paiement (2 caractères)
     */
    @Setter
    @Getter
    private String lgue;

    /**
     * ex: f97861e0f3e296b7eece2cfd86dc46c43ac88049
     */
    private String MAC;

    /**
     * Informations relatives à la commande au format JSON en Base64
     */
    @Setter
    @Getter
    private String contexte_commande;

    /**
     *
     */
    @Setter
    @Getter
    private String societe;

    @Setter
    @Getter
    @JsonProperty("texte-libre")
    private String texteLibre;

    /**
     * Email du client réalisant la transaction
     */
    @Setter
    @Getter
    private String mail;

    /**
     * URL par laquelle l’acheteur revient sur le site du commerçant suite à un paiement accepté
     */
    @Setter
    @Getter
    @JsonProperty("url_retour_ok")
    private String urlRetourOk;

    /**
     * URL par laquelle l’acheteur revient sur le site du commerçant suite à un paiement échoué
     */
    @Setter
    @Getter
    @JsonProperty("url_retour_err")
    private String urlRetourErr;


    private String ThreeDSecureChallenge;

    @Setter
    private String mode_affichage;
    @Setter
    @Getter
    private String nbrech = "";
    @Setter
    @Getter
    private String dateech1 = "";
    @Setter
    @Getter
    private String montantech1 = "";
    @Setter
    @Getter
    private String dateech2 = "";
    @Setter
    @Getter
    private String montantech2 = "";
    @Setter
    @Getter
    private String dateech3 = "";
    @Setter
    @Getter
    private String montantech3 = "";
    @Setter
    @Getter
    private String dateech4 = "";
    @Setter
    @Getter
    private String montantech4 = "";
    @Setter
    @Getter
    private String libelleMonetique;
    @Setter
    @Getter
    private String libelleMonetiqueLocalite;

    public PaiementDTO() {
    }

    public PaiementDTO(String lgue) {
        this();
        this.lgue = lgue;
    }

    @JsonGetter("TPE")
    public String getTPE() {
        return TPE;
    }

    @JsonSetter("TPE")
    public void setTPE(String TPE) {
        this.TPE = TPE;
    }

    @JsonGetter("MAC")
    public String getMAC() {
        return MAC;
    }

    @JsonSetter("MAC")
    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    @JsonGetter("ThreeDSecureChallenge")
    public String getThreeDSecureChallenge() {
        return ThreeDSecureChallenge;
    }

    @JsonSetter("ThreeDSecureChallenge")
    public void setThreeDSecureChallenge(String threeDSecureChallenge) {
        this.ThreeDSecureChallenge = threeDSecureChallenge;
    }

    public String getMode_affichage() {
        if (mode_affichage == null) {
            return "";
        }
        return mode_affichage;
    }

}

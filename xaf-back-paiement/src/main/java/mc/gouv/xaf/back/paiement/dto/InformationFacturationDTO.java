package mc.gouv.xaf.back.paiement.dto;

/**
 * DTO regroupant les paramètres nécessaires à la création d'un facture dans CIR, configurable selon les données des démarches.
 *
 * @author mboutelier.ext
 */
public class InformationFacturationDTO {

    private String nomTitulaire;

    private String prenomTitulaire;

    private String emailUsager;

    public String getNomTitulaire() {
        return nomTitulaire;
    }

    public void setNomTitulaire(String nomTitulaire) {
        this.nomTitulaire = nomTitulaire;
    }

    public String getPrenomTitulaire() {
        return prenomTitulaire;
    }

    public void setPrenomTitulaire(String prenomTitulaire) {
        this.prenomTitulaire = prenomTitulaire;
    }

    public String getEmailUsager() {
        return emailUsager;
    }

    public void setEmailUsager(String emailUsager) {
        this.emailUsager = emailUsager;
    }
}

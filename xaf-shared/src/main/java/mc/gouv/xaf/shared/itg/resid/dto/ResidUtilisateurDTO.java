package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidUtilisateurDTO implements Serializable {

    private static final long serialVersionUID = 59849805587804217L;

    private String utilisateurNomPrenom;

    private String utilisateurMatricule;

    public String getUtilisateurNomPrenom() {
        return utilisateurNomPrenom;
    }

    public void setUtilisateurNomPrenom(String utilisateurNomPrenom) {
        this.utilisateurNomPrenom = utilisateurNomPrenom;
    }

    public String getUtilisateurMatricule() {
        return utilisateurMatricule;
    }

    public void setUtilisateurMatricule(String utilisateurMatricule) {
        this.utilisateurMatricule = utilisateurMatricule;
    }
}

package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidCarteResidentDTO implements Serializable {

    private static final long serialVersionUID = 922373730003391477L;

    private ResidDemandeNouvelleCarteDTO demande;

    private ResidUtilisateurDTO utilisateur;

    private ResidUsagerDTO usager;

    public ResidDemandeNouvelleCarteDTO getDemande() {
        return demande;
    }

    public void setDemande(ResidDemandeNouvelleCarteDTO demande) {
        this.demande = demande;
    }

    public ResidUtilisateurDTO getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(ResidUtilisateurDTO utilisateur) {
        this.utilisateur = utilisateur;
    }

    public ResidUsagerDTO getUsager() {
        return usager;
    }

    public void setUsager(ResidUsagerDTO usager) {
        this.usager = usager;
    }
}

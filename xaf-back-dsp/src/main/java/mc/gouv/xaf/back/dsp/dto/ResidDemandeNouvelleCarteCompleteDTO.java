package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeNouvelleCarteCompleteDTO implements Serializable {

    private static final long serialVersionUID = 922373730003391477L;

    private ResidDemandeNouvelleCarteDTO demande;

    private ResidUtilisateurDTO utilisateur;

    private ResidUsagerNouveauDTO usager;

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

    public ResidUsagerNouveauDTO getUsager() {
        return usager;
    }

    public void setUsager(ResidUsagerNouveauDTO usager) {
        this.usager = usager;
    }
}

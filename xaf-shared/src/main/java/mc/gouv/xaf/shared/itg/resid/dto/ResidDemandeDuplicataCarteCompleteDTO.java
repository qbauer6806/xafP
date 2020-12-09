package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeDuplicataCarteCompleteDTO extends ResidDemandeCompleteDTO implements Serializable {

    private static final long serialVersionUID = 783129793599746559L;

    private ResidDemandeDuplicataCarteDTO demande;

    private ResidUtilisateurDTO utilisateur;

    private ResidUsagerDTO usager;

    public ResidDemandeDuplicataCarteDTO getDemande() {
        return demande;
    }

    public void setDemande(ResidDemandeDuplicataCarteDTO demande) {
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

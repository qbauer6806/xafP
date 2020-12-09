package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeChangementSituationCompleteDTO extends ResidDemandeCompleteDTO implements Serializable {

    private static final long serialVersionUID = 783129793599746559L;

    private ResidDemandeChangementSituationDTO demande;

    private ResidUtilisateurDTO utilisateur;

    private ResidUsagerDTO usager;

    public ResidDemandeChangementSituationDTO getDemande() {
        return demande;
    }

    public void setDemande(ResidDemandeChangementSituationDTO demande) {
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

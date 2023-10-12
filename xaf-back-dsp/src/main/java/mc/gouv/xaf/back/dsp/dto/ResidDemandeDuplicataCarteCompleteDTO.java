package mc.gouv.xaf.back.dsp.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeDuplicataCarteCompleteDTO extends ResidDemandeCompleteDTO implements Serializable {

    private static final long serialVersionUID = 783129793599746559L;

    private ResidDemandeDuplicataCarteDTO demande;

    private ResidUtilisateurDTO utilisateur;

    private ResidUsagerExistantDTO usager;

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

    public ResidUsagerExistantDTO getUsager() {
        return usager;
    }

    public void setUsager(ResidUsagerExistantDTO usager) {
        this.usager = usager;
    }
}

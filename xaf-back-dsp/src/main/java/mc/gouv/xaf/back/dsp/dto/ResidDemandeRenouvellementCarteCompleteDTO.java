package mc.gouv.xaf.back.dsp.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeRenouvellementCarteCompleteDTO extends ResidDemandeCompleteDTO implements Serializable {

    private static final long serialVersionUID = -200073602129896052L;

    private ResidDemandeRenouvellementCarteDTO demande;

    private ResidUtilisateurDTO utilisateur;

    private ResidUsagerExistantDTO usager;

    public ResidDemandeRenouvellementCarteDTO getDemande() {
        return demande;
    }

    public void setDemande(ResidDemandeRenouvellementCarteDTO demande) {
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

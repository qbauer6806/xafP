package mc.gouv.xaf.back.dsp.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeCertificatResidenceCompleteDTO extends ResidDemandeCompleteDTO implements Serializable {

    private static final long serialVersionUID = 325263472014721426L;

    private ResidDemandeCertificatResidenceDTO demande;

    private ResidUtilisateurDTO utilisateur;

    private ResidUsagerExistantDTO usager;

}

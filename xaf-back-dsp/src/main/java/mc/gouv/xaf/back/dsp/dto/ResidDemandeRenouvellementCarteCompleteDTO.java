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
public class ResidDemandeRenouvellementCarteCompleteDTO extends ResidDemandeCompleteDTO implements Serializable {

    private static final long serialVersionUID = -200073602129896052L;

    private ResidDemandeRenouvellementCarteDTO demande;

    private ResidUtilisateurDTO utilisateur;

    private ResidUsagerExistantDTO usager;

}

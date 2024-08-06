package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.enums.common.ResidTypeUsagerEnum;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidUsagerExistantDTO implements Serializable {

    private static final long serialVersionUID = 759419710680261692L;

    private ResidTypeUsagerEnum usagerType;

    private ResidResidentDTO identification;

    private ResidIdentiteExistanteDTO identite;

    private ResidAdresseDTO adresseMonaco;

    private ResidAdresseDTO adresseProvenance;

    private ResidLogementDTO logement;

    private ResidHebergeantDTO hebergeant;

    private ResidMoyensExistenceDTO moyensExistence;

    private List<ResidMembreFoyerDTO> membresFoyer;

}

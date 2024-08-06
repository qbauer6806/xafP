package mc.gouv.xaf.back.dsp.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.enums.common.ResidSituationEnum;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidMoyensExistenceDTO implements Serializable {

    private static final long serialVersionUID = -5756654094218754527L;

    private ResidSituationEnum situationPrincipale;

    private String employeurRaisonSociale;

    private String employeurVille;

    private String employeurPays;

}

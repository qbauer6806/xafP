package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.enums.common.ResidPieceJustificativeTypeEnum;

import java.io.Serializable;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidPieceJustificativeDTO implements Serializable {

    private static final long serialVersionUID = 9160763384281964658L;

    private ResidPieceJustificativeTypeEnum type;

    private String nomFichier;

    private String numero;

    private String dateDebutValidite;

    private String dateFinValidite;

}

package mc.gouv.xaf.back.dsp.dto.dlnuf;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.dsp.enums.common.ResidTypePieceIdentiteEnum;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ResidNationalite1Et2DTO implements Serializable {

    private static final long serialVersionUID = 8927813265707011626L;

    private String nationalite1;

    private ResidTypePieceIdentiteEnum typePiece;

    private String numeroPiece;

    private String dateDelivrance;

    private String dateFinValidite;

    private String paysDelivrance;

    private String nationalite2;

    private boolean ressortissant;

}

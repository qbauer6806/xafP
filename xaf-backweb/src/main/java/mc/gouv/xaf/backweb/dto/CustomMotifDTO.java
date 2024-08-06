package mc.gouv.xaf.backweb.dto;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.shared.dto.MotifDTO;

@Setter
@Getter
public class CustomMotifDTO extends MotifDTO {

    private String libelleFr;
    private String libelleEn;

    private String commentairePrerempliFr;
    private String commentairePrerempliEn;

}

package mc.gouv.xaf.front.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomRequestRechercheDTO {

    private String action;
    private CustomRequestRechercheDataDTO data;

}

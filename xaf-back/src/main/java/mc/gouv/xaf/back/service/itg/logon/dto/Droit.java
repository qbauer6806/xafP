package mc.gouv.xaf.back.service.itg.logon.dto;

import java.io.Serializable;
import lombok.Data;

@Data
public class Droit implements Serializable {

    private Integer id;
    private String code;
    private String titre;

}

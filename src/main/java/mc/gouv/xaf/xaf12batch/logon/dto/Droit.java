package mc.gouv.xaf.xaf12batch.logon.dto;

import java.io.Serializable;
import lombok.Data;

@Data
public class Droit implements Serializable {

    private Integer id;
    private String code;
    private String titre;

}

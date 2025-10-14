package mc.gouv.xaf.back.service.itg.logon.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Droit implements Serializable {

    private Integer id;
    private String code;
    private String titre;

}

package mc.gouv.xaf.back.service.itg.logon.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Set;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Role implements Serializable {

    private Integer id;
    private Appli appli;
    private String code;
    private String titre;
    private Set<Droit> droits;
}

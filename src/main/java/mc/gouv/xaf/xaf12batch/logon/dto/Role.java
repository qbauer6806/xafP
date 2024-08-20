package mc.gouv.xaf.xaf12batch.logon.dto;

import java.io.Serializable;
import java.util.Set;
import lombok.Data;

@Data
public class Role implements Serializable {

    private Integer id;
    private Appli appli;
    private String code;
    private String titre;
    private Set<Droit> droits;
}

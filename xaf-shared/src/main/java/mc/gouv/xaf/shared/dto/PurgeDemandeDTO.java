package mc.gouv.xaf.shared.dto;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PurgeDemandeDTO {

    private String identifiantDemande;

    private String statutFinal;

    private Date dateStatutFinal;

    private Date dateSuppression;

}

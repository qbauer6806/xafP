package mc.gouv.xaf.back.data.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class StatistiqueSubsetDTO {
    private String demandeId;

    private String statutPublic;

    private Date dateDernierStatut;

    private Date dateSuppression;

}

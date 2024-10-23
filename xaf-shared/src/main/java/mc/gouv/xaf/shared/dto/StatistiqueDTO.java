package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.shared.enums.TypeConnexionUsagerEnum;

import java.util.Date;

/**
 * Modélise d'un statistique
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatistiqueDTO {

    private Integer pkStatistiques;

    private Integer demandeId;

    private String statutPublic;

    private String canal;

    private Date date;

    private String demarcheId;

    private String identifiantDemande;

    private TypeConnexionUsagerEnum typeConnexionUsager;

}

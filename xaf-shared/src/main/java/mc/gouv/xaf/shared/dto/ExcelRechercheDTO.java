package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.List;

/**
 * DTO Interne permettant de regrouper les critères de recherche pour un export excel
 */
@Setter
@Getter
public class ExcelRechercheDTO {

    private Date creationStartDate;

    private Date creationEndDate;

    private List<String> statuts;

    private DataRechercheDTO data;

    private ConfigRechercheDTO config;

}

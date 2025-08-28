package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

/**
 * DTO Interne permettant de regrouper les critères de recherche pour un export excel
 */
@Setter
@Getter
public class ExcelRechercheDTO {

    private Date creationStartDate;

    private Date creationEndDate;

    private String statut;

    private DataRechercheDTO data;

}

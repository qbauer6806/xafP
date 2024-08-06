package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO Interne permettant de regrouper les critères de recherche pour un export excel
 */
@Setter
@Getter
public class ExcelRechercheDTO {

    private String creationStartDate;

    private String creationEndDate;

    private String dateAcceptationStartDate;

    private String dateAcceptationEndDate;

    private String statut;

}

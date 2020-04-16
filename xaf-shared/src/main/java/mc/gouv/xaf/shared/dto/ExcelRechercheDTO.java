package mc.gouv.xaf.shared.dto;

import java.util.Date;

/**
 * DTO Interne permettant de regrouper les critères de recherche pour un export excel
 */
public class ExcelRechercheDTO {

    private String creationStartDate;

    private String creationEndDate;

    private String statut;

    public String getCreationStartDate() {
        return creationStartDate;
    }

    public void setCreationStartDate(String creationStartDate) {
        this.creationStartDate = creationStartDate;
    }

    public String getCreationEndDate() {
        return creationEndDate;
    }

    public void setCreationEndDate(String creationEndDate) {
        this.creationEndDate = creationEndDate;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}

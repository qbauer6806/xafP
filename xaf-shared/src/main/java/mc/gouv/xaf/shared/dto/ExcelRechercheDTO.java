package mc.gouv.xaf.shared.dto;

/**
 * DTO Interne permettant de regrouper les critères de recherche pour un export excel
 */
public class ExcelRechercheDTO {

    private String creationStartDate;

    private String creationEndDate;

    private String dateAcceptationStartDate;

    private String dateAcceptationEndDate;

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

    public String getDateAcceptationStartDate() {
        return dateAcceptationStartDate;
    }

    public void setDateAcceptationStartDate(String dateAcceptationStartDate) {
        this.dateAcceptationStartDate = dateAcceptationStartDate;
    }

    public String getDateAcceptationEndDate() {
        return dateAcceptationEndDate;
    }

    public void setDateAcceptationEndDate(String dateAcceptationEndDate) {
        this.dateAcceptationEndDate = dateAcceptationEndDate;
    }
}

package mc.gouv.xaf.shared.dto;

public class PageNewParamDTO {

    /**
     * Numéro de la page (commençant par 0)
     */
    private int page;

    /**
     * nombre d'éléments par page
     */
    private int itemsPerPage;

    private String sortBy;
    private String sortDirection;

    public PageNewParamDTO(int page, int itemsPerPage, String sortBy, String sortDirection) {
        this.page = page;
        this.itemsPerPage = itemsPerPage;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }


    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
}

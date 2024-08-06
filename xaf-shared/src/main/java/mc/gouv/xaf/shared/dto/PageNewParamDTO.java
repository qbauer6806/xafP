package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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

}

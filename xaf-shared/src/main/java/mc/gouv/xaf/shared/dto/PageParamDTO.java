package mc.gouv.xaf.shared.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PageParamDTO {

    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT = "identifiant";
    private static final String ASC = "ASC";
    private static final String DESC = "DESC";
    private static final String DEFAULT_LANG = "fr";

    /**
     * Numéro de la page (commençant par 0)
     */
    @Setter
    private int page;

    /**
     * nombre d'élément par page
     */
    @Setter
    private int size;

    /**
     * nom de la propriété sur lequel faire un tri
     */
    @Setter
    private String sort;

    /**
     * direction du tri 'ASC' ou 'DESC'
     */
    private String direction;

    /**
     * tableau contenant les codes du statut sur lequel effectuer un filtre
     */
    @Setter
    private List<String> status;

    @Setter
    private List<String> statusSimplifie;

    /**
     * La langue courante de la page pour pouvoir effectuer correctement le tri sur les statut
     */
    @Setter
    private String lang;

    public PageParamDTO() {
        this.page = 0;
        this.size = DEFAULT_SIZE;
        this.sort = DEFAULT_SORT;
        this.direction = ASC;
        this.status = null;
        this.statusSimplifie = null;
        this.lang = DEFAULT_LANG;
    }

    public PageParamDTO(int page, int size, String sort, String direction, List<String> status, String lang,
            List<String> statusSimplifie) {
        this.page = page;
        this.size = size;
        this.sort = sort;
        setDirection(direction);
        this.status = status;
        this.statusSimplifie = statusSimplifie;
        this.lang = lang;
    }

    public void setDirection(String direction) {
        this.direction = DESC.equalsIgnoreCase(direction) ? DESC : ASC;
    }

}

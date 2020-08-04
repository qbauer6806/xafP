package mc.gouv.xaf.shared.dto;

import com.google.gson.Gson;

public class PageParamDTO {

    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT = "identifiant";
    private static final String ASC = "ASC";
    private static final String DESC = "DESC";
    private static final String DEFAULT_LANG = "fr";

    /**
     * Numéro de la page (commençant par 0)
     */
    private int page;

    /**
     * nombre d'élément par page
     */
    private int size;

    /**
     * nom de la propriété sur lequel faire un tri
     */
    private String sort;

    /**
     * direction du tri 'ASC' ou 'DESC'
     */
    private String direction;

    /**
     * tableau contenant les codes du statut sur lequel effectuer un filtre
     */
    private String status;

    /**
     * La langue courante de la page pour pouvoir effectuer correctement le tri sur les statut
     */
    private String lang;

    public PageParamDTO() {
        this.page = 0;
        this.size = DEFAULT_SIZE;
        this.sort = DEFAULT_SORT;
        this.direction = ASC;
        this.status = "[]";
        this.lang = DEFAULT_LANG;
    }

    public PageParamDTO(int page, int size, String sort, String direction, String status, String lang) {
        this.page = page;
        this.size = size;
        this.sort = sort;
        setDirection(direction);
        this.status = status;
        this.lang = lang;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = DESC.equalsIgnoreCase(direction) ? DESC : ASC;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String[] getStatusArray() {
        Gson gson = new Gson();
        return gson.fromJson(status, String[].class);
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}

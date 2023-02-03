package mc.gouv.xaf.back.data.es.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @deprecated les jointures seront supprimées dans ES8
 */
@Deprecated(forRemoval = true)
public class DemandeJoinFieldEsDTO {

    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String parent;

    public DemandeJoinFieldEsDTO(String name, String parent) {
        super();
        this.name = name;
        this.parent = parent;
    }

    public DemandeJoinFieldEsDTO(String name) {
        super();
        this.name = name;
    }

    public DemandeJoinFieldEsDTO() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

}

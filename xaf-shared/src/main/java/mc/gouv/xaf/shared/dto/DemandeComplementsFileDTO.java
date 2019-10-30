package mc.gouv.xaf.shared.dto;

import javax.validation.constraints.NotNull;

/**
 * Modélise un fichier d'une demande d'informations complémentaires
 * 
 * @author qdeme
 *
 */
public class DemandeComplementsFileDTO {

    @NotNull
    private String name;

    @NotNull
    private String url;

    private String meta;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

}

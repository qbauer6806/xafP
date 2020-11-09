package mc.gouv.xaf.shared.dto;

import javax.validation.constraints.NotNull;

/**
 * Modélise un fichier d'une demande d'informations complémentaires
 *
 * @author qdeme
 */
public class DemandeComplementsFileDTO {

    private Integer pkDemandesComplementsFiles;

    @NotNull
    private String name;

    @NotNull
    private String url;

    private String meta;

    private String typedoc;

    public Integer getPkDemandesComplementsFiles() {
        return pkDemandesComplementsFiles;
    }

    public void setPkDemandesComplementsFiles(Integer pkDemandesComplementsFiles) {
        this.pkDemandesComplementsFiles = pkDemandesComplementsFiles;
    }

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

    public String getTypedoc() {
        return typedoc;
    }

    public void setTypedoc(String typedoc) {
        this.typedoc = typedoc;
    }
}

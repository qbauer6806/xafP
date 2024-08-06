package mc.gouv.xaf.shared.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Modélise un fichier d'une demande d'informations complémentaires
 *
 * @author qdeme
 */
@Setter
@Getter
public class DemandeComplementsFileDTO {

    private Integer pkDemandesComplementsFiles;

    @NotNull
    private String name;

    @NotNull
    private String url;

    private String meta;

    private String typedoc;

    private boolean verification;

    private String contenu;

}

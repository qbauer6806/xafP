package mc.gouv.xaf.shared.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * Modélise un fichier lié à une demande
 *
 * @author qdeme
 */
@Setter
@Getter
public class DemandeFileDTO {

    protected Integer pkDemandesFiles;
    @NotNull
    protected String name;
    @NotNull
    protected String url;
    protected String meta;
    protected Date date;
    protected String identifiant;
    protected String typedoc;
    private Integer pkDemandesComplementsFiles;

    // Dixit mboutelier.ext : "Flag pour indiquer si c'est un fichier d'une demande complémentaire"
    // Ne va pas jusqu'en base
    private boolean compFile;

    // Correspond à la checkbox de vérification de pièces jointes dans le BO
    // Va jusqu'en base
    private boolean verification;

    private String contenu;

}

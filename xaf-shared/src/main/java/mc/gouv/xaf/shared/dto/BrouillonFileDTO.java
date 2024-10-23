package mc.gouv.xaf.shared.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Modélise un fichier lié à un brouillon d'une demande
 *
 * @author qdeme
 */
@Setter
@Getter
@ToString
public class BrouillonFileDTO {

    protected Integer pkBrouillonsFiles;

    @NotNull
    protected String name;

    @NotNull
    protected String url;

    protected String meta;

    protected Date date;

    protected String typedoc;

}


package mc.gouv.xaf.back.service.itg.file.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 *
 * DTO permettant d'indiquer une liste de fichiers à supprimer
 *
 * @author qdeme
 *
 */
@Data
public class FileBatchDTO {

    private String account;

    private String container;

    @NotNull
    private List<String> files;
}

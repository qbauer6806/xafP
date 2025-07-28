package mc.gouv.xaf.back.service.itg.file.service.dto;

import lombok.Data;
import java.util.List;

/**
 *
 * DTO permettant de donner au client appelant la liste des fichiers n'ayant
 * pas pu être supprimés, lors d'une suppression batch
 *
 * @author qdeme
 *
 */
@Data
public class FileBatchResponseDTO {

    private Integer nbFilesToDelete;

    private Integer nbFilesNotDeleted;

    private List<String> filesNotDeleted;

}

package mc.gouv.xaf.back.service.itg.file.service.dto;

import lombok.Data;

/**
 * Classe pour modélisation JSON d'une réponse de WS
 * 
 * @author qdeme
 *
 */
@Data
public class FileResponseDTO {

    /**
     * Message à retourner
     */
    private String message;
}

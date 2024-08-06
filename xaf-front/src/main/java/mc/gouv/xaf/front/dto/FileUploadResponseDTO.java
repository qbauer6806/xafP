package mc.gouv.xaf.front.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author qdeme
 * 
 */
@Setter
@Getter
public class FileUploadResponseDTO {
    
    private String fileId;
    
    public FileUploadResponseDTO(String fileId) {
        this.fileId = fileId;
    }

}

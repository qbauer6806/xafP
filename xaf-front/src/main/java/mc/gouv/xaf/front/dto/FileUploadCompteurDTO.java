package mc.gouv.xaf.front.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileUploadCompteurDTO {

    private int compteur;
    private LocalDateTime datePremierUpload;

}

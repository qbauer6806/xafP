package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UploadFileDTO {

    private String nom;
    private String type;
    private boolean visibilite;

}

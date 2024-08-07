package mc.gouv.xaf.xaf12batch.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DemandeFileDTO {
    private String typeFichier;
    private String content;
    private String url;

}

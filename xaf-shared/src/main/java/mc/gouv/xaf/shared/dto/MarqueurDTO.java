package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Modélise un marqueur
 *
 * @author mpavone
 */
@Setter
@Getter
public class MarqueurDTO {

    private Integer pkMarqueur;
    private String description;
    private String identifiant;
    private String chemin;
    private String buildId;

}

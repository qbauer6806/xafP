package mc.gouv.xaf.backweb.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Modélise un marqueur
 *
 * @author mpavone
 */
@Setter
@Getter
public class ConfigDTO {

    private String buildId;
    private String date;
    private String version;
    private String wysiwygVersion;
}

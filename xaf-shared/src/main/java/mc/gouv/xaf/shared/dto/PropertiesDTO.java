package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mc.gouv.xaf.shared.enums.PropertiesTypeEnum;

/**
 * Modélise une donnée d'un properties
 *
 * @author mboutelier.ext
 */
@Setter
@Getter
@NoArgsConstructor
public class PropertiesDTO {

    private Integer pkProperties;

    private String demarcheId;

    private PropertiesTypeEnum type;

    private String key;

    private String descriptif;

    private String value;

    public PropertiesDTO(String key, String value) {
        this.key = key;
        this.value = value;
    }

}

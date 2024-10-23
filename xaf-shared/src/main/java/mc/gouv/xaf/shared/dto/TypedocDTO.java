package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO pour la gestion des types de documents
 *
 * @author mboutelier.ext
 */
@Setter
@Getter
public class TypedocDTO {

    private String key;

    private String value;

    private boolean enabled;

    public TypedocDTO(String key, String value, boolean enabled) {
        this.key = key;
        this.value = value;
        this.enabled = enabled;
    }

}

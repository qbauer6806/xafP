package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PropertiesListEntityDTO {

    private String id;
    private String label;
    private boolean enabled;
    private boolean editable;

}

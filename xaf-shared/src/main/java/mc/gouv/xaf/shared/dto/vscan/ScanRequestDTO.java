package mc.gouv.xaf.shared.dto.vscan;

import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class ScanRequestDTO {
    private String codeAppli;
    private String fileName;
    private String enduserAppModule;
    private Set<MetadataDTO> metadata;
}

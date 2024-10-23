package mc.gouv.xaf.shared.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileSubCategoryDTO {

    private String key;

    private String name;

    private List<DemandeFileDTO> files;

    private boolean typedoc;

}

package mc.gouv.xaf.back.data.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExportImportConfigPropertyDTO {

    private String name;
    private String label;
    private String categoryName;
    private boolean enabled;
    private boolean editable;

}

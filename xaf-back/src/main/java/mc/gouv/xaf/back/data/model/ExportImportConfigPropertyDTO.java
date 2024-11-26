package mc.gouv.xaf.back.data.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExportImportConfigPropertyDTO {

    private String name;
    private String label;
    private String categoryName;
    private boolean enabled;
    private boolean editable;

}

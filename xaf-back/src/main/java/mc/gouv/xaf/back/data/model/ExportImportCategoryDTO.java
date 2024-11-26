package mc.gouv.xaf.back.data.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExportImportCategoryDTO {

    private String label;
    private boolean editable;

}

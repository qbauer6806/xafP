package mc.gouv.xaf.back.data.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExportImportCategoryDTO {

    private String label;
    private boolean editable;

    public ExportImportCategoryDTO(String label, boolean editable) {
        this.label = label;
        this.editable = editable;
    }

}

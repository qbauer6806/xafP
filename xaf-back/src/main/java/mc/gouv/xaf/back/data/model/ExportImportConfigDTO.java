package mc.gouv.xaf.back.data.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExportImportConfigDTO {

    private List<ExportImportConfigPropertyDTO> properties = new ArrayList<>();
    private List<ExportImportCategoryDTO> categories = new ArrayList<>();

}

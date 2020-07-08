package mc.gouv.xaf.back.data.es.model;

import java.util.ArrayList;
import java.util.List;

public class ExportImportConfigDTO {

    private List<ExportImportConfigPropertyDTO> properties = new ArrayList<>();
    private List<ExportImportCategoryDTO> categories = new ArrayList<>();

    public List<ExportImportConfigPropertyDTO> getProperties() {
        return properties;
    }

    public void setProperties(List<ExportImportConfigPropertyDTO> properties) {
        this.properties = properties;
    }

    public List<ExportImportCategoryDTO> getCategories() {
        return categories;
    }

    public void setCategories(List<ExportImportCategoryDTO> categories) {
        this.categories = categories;
    }

}

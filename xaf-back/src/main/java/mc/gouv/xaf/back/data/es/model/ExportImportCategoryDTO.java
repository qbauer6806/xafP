package mc.gouv.xaf.back.data.es.model;

public class ExportImportCategoryDTO {

    private String label;
    private boolean editable;

    public ExportImportCategoryDTO() {
        super();
    }

    public ExportImportCategoryDTO(String label, boolean editable) {
        super();
        this.label = label;
        this.editable = editable;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

}

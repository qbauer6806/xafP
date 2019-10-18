package mc.gouv.xaf.back.data.es.model;

public class ConfigPropertyDTO {

    private String name;
    private String label;
    private Integer categoryId;
    private boolean enabled;

    public ConfigPropertyDTO() {
        super();
    }

    public ConfigPropertyDTO(String name, String label, Integer categoryId, boolean enabled) {
        super();
        this.name = name;
        this.label = label;
        this.categoryId = categoryId;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}

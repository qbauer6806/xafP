package mc.gouv.af.back.data.es.model;

import java.util.ArrayList;
import java.util.List;

public class EsProperty {

    private String name;
    private String type;
    private List<String> fields = new ArrayList<>();
    private String label;
    private Integer categoryId;
    private List<EsCategory> allCategories = new ArrayList<>();
    private boolean enabled = true;
    private boolean editable;

    public static final String BOOLEAN_TYPE = "boolean";

    public EsProperty(String name) {
        super();
        this.name = name;
    }

    public EsProperty(String name, List<String> fields) {
        super();
        this.name = name;
        this.fields = fields;
    }

    public EsProperty(String name, String type, List<String> fields) {
        super();
        this.name = name;
        this.type = type;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
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

    public void addField(String fieldName) {
        fields.add(fieldName);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<EsCategory> getAllCategories() {
        return allCategories;
    }

    public void setAllCategories(List<EsCategory> allCategories) {
        this.allCategories = allCategories;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EsProperty other = (EsProperty) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DemandeEsField [name=" + name + ", fields=" + fields + "]";
    }

}

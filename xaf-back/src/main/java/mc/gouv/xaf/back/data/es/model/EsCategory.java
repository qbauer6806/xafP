package mc.gouv.xaf.back.data.es.model;

public class EsCategory implements Comparable<EsCategory> {

    private Integer id;
    private String label;
    private boolean editable;

    public EsCategory() {
    }

    public EsCategory(Integer id, String label, boolean editable) {
        super();
        this.id = id;
        this.label = label;
        this.editable = editable;
    }

    public EsCategory(Integer id, String label) {
        super();
        this.id = id;
        this.label = label;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    @Override
    public int compareTo(EsCategory o) {

        if (o == null) {
            return -1;
        }
        if (this.label == null) {
            return 1;
        }

        return this.label.compareTo(o.getLabel());
    }

}

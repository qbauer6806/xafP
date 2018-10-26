package mc.gouv.af.back.data.es.model;

public class DemandesFacet {

    private String name;
    private long size;

    public DemandesFacet(String name, long size) {
        super();
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "DemandesFacet [name=" + name + ", size=" + size + "]";
    }

}

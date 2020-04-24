package mc.gouv.xaf.backweb.dto;

/**
 * @author qdeme
 */
public class AutocompleteUsagerDTO implements Comparable<AutocompleteUsagerDTO> {

    private String value;

    private String data;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public int compareTo(AutocompleteUsagerDTO o) {
        return this.getValue().compareTo(o.getValue());
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}

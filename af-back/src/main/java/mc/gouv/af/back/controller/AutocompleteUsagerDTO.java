package mc.gouv.af.back.controller;

/**
 * 
 * @author qdeme
 *
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

}

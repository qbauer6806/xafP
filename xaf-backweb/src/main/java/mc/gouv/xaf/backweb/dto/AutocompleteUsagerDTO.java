package mc.gouv.xaf.backweb.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author qdeme
 */
@Setter
@Getter
public class AutocompleteUsagerDTO implements Comparable<AutocompleteUsagerDTO> {

    private String value;

    private String data;

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

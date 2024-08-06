package mc.gouv.xaf.back.data.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class RechercheCategoryDTO implements Comparable<RechercheCategoryDTO> {

    private Integer id;
    private String label;
    private boolean editable;

    @Override
    public int compareTo(RechercheCategoryDTO o) {

        if (o == null) {
            return -1;
        }
        if (this.label == null) {
            return 1;
        }

        return this.label.compareTo(o.getLabel());
    }

}

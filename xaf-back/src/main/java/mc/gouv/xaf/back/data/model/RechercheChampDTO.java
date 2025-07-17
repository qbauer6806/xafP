package mc.gouv.xaf.back.data.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@AllArgsConstructor
public class RechercheChampDTO {

    private String name;
    private String type;
    private List<String> fields = new ArrayList<>();
    private String label;
    private Integer categoryId;
    private List<RechercheCategoryDTO> allCategories = new ArrayList<>();
    private boolean enabled = true;
    private boolean editable;

    public RechercheChampDTO() {
        // Constructeur par défaut requis pour la désérialisation
    }

}

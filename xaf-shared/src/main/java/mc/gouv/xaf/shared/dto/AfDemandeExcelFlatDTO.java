package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AfDemandeExcelFlatDTO {

    protected DemandeFlatDTO generic;

    protected JsonNode contenu;

    private String etatInterne;

    public AfDemandeExcelFlatDTO(DemandeFlatDTO generic, JsonNode contenu) {
        this.generic = generic;
        this.contenu = contenu;
    }

}

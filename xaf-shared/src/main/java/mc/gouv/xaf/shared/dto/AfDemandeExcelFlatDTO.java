package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AfDemandeExcelFlatDTO {

    protected DemandeFlatDTO generic;

    protected Map<String, Object> contenu;

    private String etatInterne;

    public AfDemandeExcelFlatDTO(DemandeFlatDTO generic, JsonNode contenu) {
        this.generic = generic;
        if (contenu != null) {
            ObjectMapper mapper = new ObjectMapper();
            // Convertir JsonNode en Map, car sinon JXLS va afficher les string avec les ""
            this.contenu = mapper.convertValue(contenu, new TypeReference<>() {});
        }

    }

}

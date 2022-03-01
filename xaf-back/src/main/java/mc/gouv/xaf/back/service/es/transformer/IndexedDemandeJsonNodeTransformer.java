package mc.gouv.xaf.back.service.es.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.back.data.es.model.GenericContenuEsDTO;

public interface IndexedDemandeJsonNodeTransformer {

    /**
     * @deprecated ES n'utilise plus de transformateur JSON
     */
    JsonNode transform(JsonNode jsonNode);

    GenericContenuEsDTO buildGenericContenu(JsonNode node, String buildId);

}

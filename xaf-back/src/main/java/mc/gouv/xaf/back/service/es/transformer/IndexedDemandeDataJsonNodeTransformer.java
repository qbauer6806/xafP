package mc.gouv.xaf.back.service.es.transformer;

import com.fasterxml.jackson.databind.JsonNode;

public interface IndexedDemandeDataJsonNodeTransformer {

    JsonNode transform(JsonNode jsonNode);
}

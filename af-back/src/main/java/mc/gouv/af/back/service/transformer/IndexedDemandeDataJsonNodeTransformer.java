package mc.gouv.af.back.service.transformer;

import com.fasterxml.jackson.databind.JsonNode;

public interface IndexedDemandeDataJsonNodeTransformer {

    JsonNode transform(JsonNode jsonNode);
}

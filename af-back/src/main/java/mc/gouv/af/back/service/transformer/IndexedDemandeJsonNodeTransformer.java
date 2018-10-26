package mc.gouv.af.back.service.transformer;

import com.fasterxml.jackson.databind.JsonNode;

public interface IndexedDemandeJsonNodeTransformer {

    JsonNode transform(JsonNode jsonNode);
}

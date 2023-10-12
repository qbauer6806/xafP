package mc.gouv.xaf.back.service.es.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.shared.dto.es.GenericContenuDTO;

public interface IndexedDemandeJsonNodeTransformer {

    GenericContenuDTO buildGenericContenu(JsonNode node, String buildId);

}

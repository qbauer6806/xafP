package mc.gouv.xaf.back.service.es.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.shared.dto.es.GenericContenuEsDTO;

public interface IndexedDemandeJsonNodeTransformer {

    GenericContenuEsDTO buildGenericContenu(JsonNode node, String buildId);

}

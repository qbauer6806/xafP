package mc.gouv.xaf.back.service.es.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.back.data.entity.DemandesDataBO;
import mc.gouv.xaf.back.data.es.model.GenericDemandeDataEsDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;

import java.util.Set;

public interface IndexedDemandeDataJsonNodeTransformer {

    JsonNode transform(JsonNode jsonNode);

    GenericDemandeDataEsDTO buildDemandeDataBO(Set<DemandesDataBO> dataBOS);

    GenericDemandeDataEsDTO buildDemandeDataDTO(DemandeDataDTO[] dataDTOS);

}

package mc.gouv.xaf.back.service.postprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface PostProcessingProvider {

    DemandeDTO postprocess(DemandeDTO demande, JsonNode donneesExternes);

}

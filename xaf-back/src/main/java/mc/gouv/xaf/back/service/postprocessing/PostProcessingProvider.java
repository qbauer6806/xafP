package mc.gouv.xaf.back.service.postprocessing;

import tools.jackson.databind.JsonNode;
import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface PostProcessingProvider {

    // si post processing spécifique au TS possibilité d'override
    default DemandeDTO postprocess(DemandeDTO demande, JsonNode donneesExternes) {
        return demande;
    }

}

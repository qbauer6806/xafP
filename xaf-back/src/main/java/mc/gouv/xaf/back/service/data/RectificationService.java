package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeInputDTO;

public interface RectificationService {

     DemandeDTO updateDemande(Integer demandeId, DemandeInputDTO demande, Integer usagerId, String agentId);

}

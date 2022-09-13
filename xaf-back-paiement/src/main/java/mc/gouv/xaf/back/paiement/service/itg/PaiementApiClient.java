package mc.gouv.xaf.back.paiement.service.itg;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface PaiementApiClient {
    boolean capture(MoyenPaiementBO paiement, OperationBO operation, DemandeDTO demandeDTO);
}

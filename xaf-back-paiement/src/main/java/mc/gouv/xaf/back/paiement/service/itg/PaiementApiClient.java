package mc.gouv.xaf.back.paiement.service.itg;

import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.dto.OperationDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface PaiementApiClient {
    boolean capture(MoyenPaiementDTO paiement, OperationDTO operation, DemandeDTO demandeDTO);
}

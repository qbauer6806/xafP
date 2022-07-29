package mc.gouv.xaf.back.paiement.client;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface PaiementClient {
    boolean capture(MoyenPaiementBO paiement, OperationBO operation, DemandeDTO demandeDTO);
}

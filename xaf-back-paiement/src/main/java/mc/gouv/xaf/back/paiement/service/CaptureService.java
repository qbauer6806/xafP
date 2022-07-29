package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface CaptureService {
    OperationBO capture(MoyenPaiementBO moyenPaiementBO, DemandeDTO demandeDTO) throws Exception;

}

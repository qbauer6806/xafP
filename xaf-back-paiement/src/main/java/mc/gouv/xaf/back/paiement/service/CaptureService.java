package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.dto.OperationDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface CaptureService {
    OperationDTO capture(MoyenPaiementDTO moyenPaiementDTO, DemandeDTO demandeDTO) throws Exception;

}

package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface CaptureService {
    CommandeOperationDTO capture(CommandeDTO commandeDTO, DemandeDTO demandeDTO) throws Exception;

}

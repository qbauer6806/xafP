package mc.gouv.xaf.back.paiement.service.itg;

import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface PaiementApiClient {
    boolean capture(CommandeDTO commandeDTO, CommandeOperationDTO operation, DemandeDTO demandeDTO);
}

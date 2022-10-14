package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;

public interface TicketRecapitulatifService {

    void sendMail(CommandeOperationDTO operation, CommandeDTO commandeDTO, Integer demandeId);

}

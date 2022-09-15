package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.dto.OperationDTO;

public interface TicketRecapitulatifService {

    void sendMail(OperationDTO operation, MoyenPaiementDTO moyenPaiement, Integer demandeId);

}

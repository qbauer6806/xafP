package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;

public interface TicketRecapitulatifService {

    void sendMail(OperationBO operation, MoyenPaiementBO moyenPaiement, Integer demandeId);

}

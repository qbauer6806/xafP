package mc.gouv.xaf.back.paiement.client;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;

public interface PaiementClient {
    String capture(MoyenPaiementBO paiement, double montant) throws Exception;
}

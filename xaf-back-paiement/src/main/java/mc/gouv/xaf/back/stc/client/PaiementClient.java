package mc.gouv.xaf.back.stc.client;

import mc.gouv.xaf.back.stc.data.entity.MoyenPaiementBO;

public interface PaiementClient {
    String capture(MoyenPaiementBO paiement, double montant);
}

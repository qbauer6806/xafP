package mc.gouv.xaf.back.paiement.client;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;

public interface PaiementClient {
    void capture(MoyenPaiementBO paiement, OperationBO operation) throws Exception;
}

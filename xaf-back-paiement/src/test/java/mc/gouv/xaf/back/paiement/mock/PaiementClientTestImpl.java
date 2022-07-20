package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.paiement.client.PaiementClient;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationStatutBO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class PaiementClientTestImpl implements PaiementClient {
    @Override
    public void capture(MoyenPaiementBO paiement, OperationBO operation) {
        operation.setOperationStatut(OperationStatutBO.REFUSEE);
    }
}

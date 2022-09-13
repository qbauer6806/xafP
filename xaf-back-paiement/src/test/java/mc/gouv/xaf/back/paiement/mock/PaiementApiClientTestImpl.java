package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.paiement.service.itg.PaiementApiClient;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationStatutBO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class PaiementApiClientTestImpl implements PaiementApiClient {
    @Override
    public boolean capture(MoyenPaiementBO paiement, OperationBO operation, DemandeDTO demandeDTO) {
        operation.setOperationStatut(OperationStatutBO.ACCEPTEE);
        return true;
    }
}

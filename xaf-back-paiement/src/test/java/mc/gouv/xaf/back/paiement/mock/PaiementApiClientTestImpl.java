package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;
import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.dto.OperationDTO;
import mc.gouv.xaf.back.paiement.service.itg.PaiementApiClient;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class PaiementApiClientTestImpl implements PaiementApiClient {
    @Override
    public boolean capture(MoyenPaiementDTO paiement, OperationDTO operation, DemandeDTO demandeDTO) {
        operation.setOperationStatut(OperationStatutEnum.ACCEPTEE.name());
        return true;
    }
}

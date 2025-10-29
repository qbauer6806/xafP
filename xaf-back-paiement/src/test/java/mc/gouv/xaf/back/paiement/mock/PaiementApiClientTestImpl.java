package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.itg.monetico.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.service.itg.PaiementApiClient;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class PaiementApiClientTestImpl implements PaiementApiClient {

    @Override
    public boolean capture(CommandeDTO commandeDTO, CommandeOperationDTO operation, DemandeDTO demandeDTO) {
        operation.setOperationStatut(OperationStatutEnum.ACCEPTEE.name());
        return true;
    }
}

package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.paiement.dto.InformationFacturationDTO;
import mc.gouv.xaf.back.paiement.service.PaiementsDataProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.stereotype.Component;

@Component
public class TestsPaiementsDataDelegate implements PaiementsDataProvider {
    @Override
    public InformationFacturationDTO getInfosFacturation(DemandeDTO demandeDTO) {
        return null;
    }
}

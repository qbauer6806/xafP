package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeDemandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.dto.InformationFacturationDTO;
import mc.gouv.xaf.back.paiement.dto.itg.cir.CirRequestDTO;
import mc.gouv.xaf.back.paiement.service.PaiementsDataProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestsPaiementsDataDelegate implements PaiementsDataProvider {
    @Override
    public InformationFacturationDTO getInfosFacturation(DemandeDTO demandeDTO) {
        return null;
    }

    @Override
    public double getMontantCapture(DemandeDTO demandeDTO, CommandeDemandeDTO commandeDemandeDTO) {
        return 80.0;
    }

    @Override
    public List<CirRequestDTO> getLignesFacture(DemandeDTO demandeDTO, CommandeOperationDTO operation, CommandeDTO commandeDTO) {
        return new ArrayList<>();
    }
}

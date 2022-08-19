package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.paiement.dto.PaiementHistoriqueDTO;
import mc.gouv.xaf.back.paiement.service.PaiementHistoriqueService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaiementHistoriqueServiceTestImpl implements PaiementHistoriqueService {
    @Override
    public List<PaiementHistoriqueDTO> findAllByDemandeId(Integer demandeId) {
        return null;
    }

    @Override
    public void ajouterHistorique(PaiementHistoriqueDTO dto) {

    }

    @Override
    public void ajouterHistoriqueDebitEchec(DemandeDTO demandeDTO) {

    }

    @Override
    public void ajouterHistoriqueDebitOK(DemandeDTO demandeDTO) {

    }

    @Override
    public void ajouterHistoriqueDebitAbandonne(DemandeDTO demandeDTO) {

    }

    @Override
    public void ajouterHistoriqueEmpreinteExpiree(Integer pkDemandes) {

    }
}

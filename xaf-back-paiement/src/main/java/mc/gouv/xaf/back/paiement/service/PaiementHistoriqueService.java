package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.dto.PaiementHistoriqueDTO;

import java.util.List;

public interface PaiementHistoriqueService {
    List<PaiementHistoriqueDTO> findAllByDemandeId(Integer demandeId);
}

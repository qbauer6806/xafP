package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.dto.PaiementHistoriqueDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

import java.util.List;

public interface PaiementHistoriqueService {
    List<PaiementHistoriqueDTO> findAllByDemandeId(Integer demandeId);

    void ajouterHistorique(PaiementHistoriqueDTO dto);

    void ajouterHistoriqueDebitEchec(DemandeDTO demandeDTO);

    void ajouterHistoriqueDebitOK(DemandeDTO demandeDTO);

    void ajouterHistoriqueDebitAbandonne(DemandeDTO demandeDTO);

    void ajouterHistoriqueEmpreinteExpiree(Integer pkDemandes);
}

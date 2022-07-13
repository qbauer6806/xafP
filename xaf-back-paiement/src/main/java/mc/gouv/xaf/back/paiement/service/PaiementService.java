package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.dto.PaiementDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

import java.util.Optional;

public interface PaiementService {

    PaiementDTO create(String demandesId, String langue, Integer usagerId, boolean iframe);

    void updateStatus(String reference, String status);

    Optional<MoyenPaiementBO> getMoyenPaiement(Integer demandeId);

    String capture(MoyenPaiementBO moyenPaiementBO, DemandeDTO demandeDTO) throws Exception;

}

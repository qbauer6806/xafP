package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.dto.PaiementDTO;
import mc.gouv.xaf.shared.stc.MoyenPaiementDTO;

import java.util.Optional;

public interface PaiementService {

    PaiementDTO create(String demandesId, String langue, Integer usagerId, boolean iframe);

    void updateStatus(MoyenPaiementDTO moyenPaiementDTO);

    Optional<MoyenPaiementBO> getMoyenPaiement(Integer demandeId);


}

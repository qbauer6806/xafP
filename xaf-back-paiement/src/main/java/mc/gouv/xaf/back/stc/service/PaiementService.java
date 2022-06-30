package mc.gouv.xaf.back.stc.service;

import mc.gouv.xaf.back.stc.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.shared.stc.dto.PaiementDTO;

import java.io.IOException;
import java.util.Optional;

public interface PaiementService {

    PaiementDTO create(String demandesId, String langue, Integer usagerId);

    void updateStatus(String reference, String status);

    Optional<MoyenPaiementBO> getMoyenPaiement(Integer demandeId);

    String capture(MoyenPaiementBO moyenPaiementBO, Integer demandeId) throws IOException;

}

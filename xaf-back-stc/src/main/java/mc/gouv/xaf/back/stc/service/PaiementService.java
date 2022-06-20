package mc.gouv.xaf.back.stc.service;

import mc.gouv.xaf.back.stc.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.shared.stc.dto.PaiementDTO;

import java.io.IOException;
import java.io.InputStream;

public interface PaiementService {

    PaiementDTO create(Integer demandeId, String langue, Integer usagerId);

    void updateStatus(String reference, String status);

    MoyenPaiementBO getMoyenPaiement(Integer demandeId);

    String capture(MoyenPaiementBO moyenPaiementBO) throws IOException;

}

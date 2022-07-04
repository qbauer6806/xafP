package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.shared.stc.dto.PaiementDTO;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Optional;

public interface PaiementService {

    PaiementDTO create(String demandesId, String langue, Integer usagerId);

    void updateStatus(String reference, String status) throws IOException, SAXException;

    Optional<MoyenPaiementBO> getMoyenPaiement(Integer demandeId);

    String capture(MoyenPaiementBO moyenPaiementBO, Integer usagerId) throws IOException;

}

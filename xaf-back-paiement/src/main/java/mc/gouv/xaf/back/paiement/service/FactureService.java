package mc.gouv.xaf.back.paiement.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FactureService {

    void saveFacture(String reference, Integer demandeId) throws IOException;

    void saveRecuPaiement(String identifiantDemande, MultipartFile file);

}

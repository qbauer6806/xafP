package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.shared.paiement.infofacturation.InfoFacturationResponseDTO;
import mc.gouv.xaf.shared.paiement.tableaupaiement.TableauDTO;
import java.util.List;

public interface PaiementService {

    List<TableauDTO> getTableauPaiement(String ids, String objectType, Integer usagerId);
    InfoFacturationResponseDTO getInfoFacturation(Integer usagerId);
    void createMoyenPaiement(String demandeIds, Integer usagerId, String orderId);
}

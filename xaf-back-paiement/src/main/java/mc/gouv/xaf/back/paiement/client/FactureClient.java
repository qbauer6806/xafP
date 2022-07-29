package mc.gouv.xaf.back.paiement.client;

import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;

public interface FactureClient {

    String check(String numFacture);

    Optional<String> createFacture(String numPermis, String numImmat, Double montant, String codeTransaction, Integer usagerId, HashMap<String, Double> objetMontants, DemandeDTO demandeDTO, OperationBO operationBO);

    Optional<InputStream> getFacture(String numFacture, DemandeDTO demandeDTO);
}

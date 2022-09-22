package mc.gouv.xaf.back.paiement.service.itg;

import mc.gouv.xaf.back.paiement.dto.InformationFacturationDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.dto.itg.cir.PermisDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;

public interface FactureApiClient {

    String check(String numFacture);

    Optional<String> createFacture(String numPermis, String numImmat, Double montant, String codeTransaction, InformationFacturationDTO infoFacturation, HashMap<String, Double> objetMontants, DemandeDTO demandeDTO, CommandeOperationDTO operationdto);

    Optional<InputStream> getFacture(String numFacture, DemandeDTO demandeDTO) throws Exception;

    Optional<PermisDTO> getPermis(String numPermis) throws Exception;
}

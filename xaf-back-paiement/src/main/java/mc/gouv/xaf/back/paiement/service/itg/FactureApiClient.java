package mc.gouv.xaf.back.paiement.service.itg;

import mc.gouv.xaf.back.paiement.dto.CommandeDemandeArticleDTO;
import mc.gouv.xaf.back.paiement.dto.InformationFacturationDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.dto.itg.cir.PermisDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.apache.http.client.HttpResponseException;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface FactureApiClient {
    String INCIDENT = "#Incident";

    String check(String numFacture);

    Optional<String> createFacture(String numPermis, String numImmat, double montant, String codeTransaction, InformationFacturationDTO infoFacturation, List<CommandeDemandeArticleDTO> articles, DemandeDTO demandeDTO, CommandeOperationDTO operationdto);

    Optional<InputStream> getFacture(String numFacture, DemandeDTO demandeDTO) throws HttpResponseException;

    PermisDTO getPermis(String numPermis) throws HttpResponseException;
}

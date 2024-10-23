package mc.gouv.xaf.back.paiement.service.itg;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.http.client.HttpResponseException;

import mc.gouv.xaf.back.paiement.dto.itg.cir.CirRequestDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface FactureApiClient {

    String INCIDENT = "#Incident";

    String check(String numFacture);

    /**
     * Création d'une facture avec plusieurs lignes
     */
    Optional<String> createFacture(List<CirRequestDTO> lignes, DemandeDTO demandeDTO);

    Optional<InputStream> getFacture(String numFacture, DemandeDTO demandeDTO) throws HttpResponseException;

}

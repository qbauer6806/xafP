package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.paiement.service.itg.FactureApiClient;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.dto.InformationFacturationDTO;
import mc.gouv.xaf.back.paiement.dto.itg.cir.PermisDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;

@Primary
@Component
public class FactureApiClientTestImpl implements FactureApiClient {
    @Override
    public String check(String numFacture) {
        return null;
    }

    @Override
    public Optional<String> createFacture(String numPermis, String numImmat, Double montant, String codeTransaction, InformationFacturationDTO infoFacturation, HashMap<String, Double> objetMontants, DemandeDTO demandeDTO, OperationBO operationBO) {
        return Optional.of("facture001");
    }

    @Override
    public Optional<InputStream> getFacture(String numFacture, DemandeDTO demandeDTO) {
        return Optional.of(new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        });
    }

    @Override
    public Optional<PermisDTO> getPermis(String numPermis) {
        return Optional.of(new PermisDTO());
    }
}

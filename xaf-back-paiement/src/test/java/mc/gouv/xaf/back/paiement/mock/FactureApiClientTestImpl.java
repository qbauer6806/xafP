package mc.gouv.xaf.back.paiement.mock;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.paiement.dto.itg.cir.CirRequestDTO;
import mc.gouv.xaf.back.paiement.service.itg.FactureApiClient;
import mc.gouv.xaf.shared.dto.DemandeDTO;

@Primary
@Component
public class FactureApiClientTestImpl implements FactureApiClient {
    @Override
    public String check(String numFacture) {
        return null;
    }

    @Override
    public Optional<String> createFacture(List<CirRequestDTO> lignes, DemandeDTO demandeDTO) {
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
}

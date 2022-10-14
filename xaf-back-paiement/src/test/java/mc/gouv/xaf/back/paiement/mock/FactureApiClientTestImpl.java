package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.paiement.dto.CommandeDemandeArticleDTO;
import mc.gouv.xaf.back.paiement.dto.InformationFacturationDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.dto.itg.cir.PermisDTO;
import mc.gouv.xaf.back.paiement.service.itg.FactureApiClient;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Primary
@Component
public class FactureApiClientTestImpl implements FactureApiClient {
    @Override
    public String check(String numFacture) {
        return null;
    }

    @Override
    public Optional<String> createFacture(String numPermis, String numImmat, double montant, String codeTransaction, InformationFacturationDTO infoFacturation, List<CommandeDemandeArticleDTO> articles, DemandeDTO demandeDTO, CommandeOperationDTO commandeOperationDTO) {
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

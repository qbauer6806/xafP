package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.paiement.service.MontantService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class MontantServiceTestImpl implements MontantService {
    private static final String PERMIS = "PERMIS";

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public BigDecimal getMontant(Integer demandeId) {
        DemandeDTO demandeDto = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(),
                demandeId);
        return getMontant(demandeDto);
    }

    @Override
    public BigDecimal getMontant(DemandeDTO demandeDto) {
        return getMontant(getPaiements(demandeDto));
    }

    @Override
    public BigDecimal getMontant(Map<String, BigDecimal> objetMontants) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal montant : objetMontants.values()) {
            total = total.add(montant);
        }
        return total;
    }

    @Override
    public HashMap<String, BigDecimal> getPaiements(DemandeDTO demandeDto) {
        HashMap<String, BigDecimal> objetMontants = new HashMap<>();
        objetMontants.put(PERMIS, new BigDecimal(80));
        return objetMontants;
    }

    @Override
    public String getCodeFacturation(String montantKey) {
        return "P1";
    }
}

package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.paiement.service.MontantService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class MontantServiceTestImpl implements MontantService {
    @Override
    public Map<String, BigDecimal> getArticles(DemandeDTO demandeDto) {
        Map<String, BigDecimal> articles = new HashMap<>();
        articles.put("P1", BigDecimal.valueOf(80.0));
        return articles;
    }
}

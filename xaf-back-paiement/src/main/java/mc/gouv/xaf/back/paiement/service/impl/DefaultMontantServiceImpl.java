package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeArticleBO;
import mc.gouv.xaf.back.paiement.service.MontantService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.context.annotation.Fallback;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Fallback
public class DefaultMontantServiceImpl implements MontantService {

    @Override
    public List<CommandeDemandeArticleBO> getArticles(DemandeDTO demandeDto) {
        return List.of();
    }
}

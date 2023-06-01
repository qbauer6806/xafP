package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeArticleBO;
import mc.gouv.xaf.back.paiement.service.MontantService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MontantServiceTestImpl implements MontantService {
    @Override
    public List<CommandeDemandeArticleBO> getArticles(DemandeDTO demandeDto) {
        var articles = new ArrayList<CommandeDemandeArticleBO>();
        var article = new CommandeDemandeArticleBO();
        article.setMontant(80.0);
        article.setCodeTarif("P1");
        articles.add(article);
        return articles;
    }
}

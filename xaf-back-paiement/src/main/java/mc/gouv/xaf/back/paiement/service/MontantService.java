package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeArticleBO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

import java.util.List;

public interface MontantService {

    /**
     * <p>Permet de récupérer une liste d'articles en fontion du contenu de la demande.</p>
     * <p>L'article doit avoir :</p>
     * <ul>
     *     <li>Une chaîne de caractères contenant le code du tarif à envoyer à la facturation</li>
     *     <li>LUn double contenant le code du tarif</li>
     * </ul>
     */
    List<CommandeDemandeArticleBO> getArticles(DemandeDTO demandeDto);

}

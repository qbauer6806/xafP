package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.shared.dto.DemandeDTO;

import java.math.BigDecimal;
import java.util.Map;

public interface MontantService {

    /**
     * <p>Permet de récuperer une map contenant les articles en fontion du contenu de la demande.</p>
     * <p>Format de la map :</p>
     * <ul>
     *     <li>Clé : Chaîne de caractères contenant le code du tarif à envoyer à la facturation</li>
     *     <li>Valeur : BigDecimal contenant le montant de l'article</li>
     * </ul>
     */
    Map<String, BigDecimal> getArticles(DemandeDTO demandeDto);

}

package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.PaiementHistoriqueBO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * Classe de repo pour l'historique du Paiement.
 *
 * @author mboutelier.ext
 */
// On désactive la règle de Sonar sur le nommage des méthodes, car pour construire des requêtes on est obligé de mettre des '_'
@SuppressWarnings("java:S100")
public interface PaiementHistoriqueRepository extends JpaRepository<PaiementHistoriqueBO, Integer> {

    List<PaiementHistoriqueBO> findByFkDemandesPkDemandesOrderByDateDesc(Integer pkDemandes);
    PaiementHistoriqueBO findByFkDemandesPkDemandesAndStatut(Integer pkDemandes, String statut);

    List<PaiementHistoriqueBO> findByFkDemandes_PkDemandesIn(Collection<Integer> pkDemandes);

    void deleteByFkDemandes_PkDemandesIn(Collection<Integer> pkDemandes);

}

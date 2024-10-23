package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// On désactive la règle de Sonar sur le nommage des méthodes, car pour construire des requêtes on est obligé de mettre des '_'
@SuppressWarnings("java:S100")
public interface CommandeRepository extends JpaRepository<CommandeBO, Integer> {

    /**
     * Récupère la liste des commandes liées à une demande
     *
     * @param demandeId,
     *         la clé primaire de la demande
     * @return une liste contenant les commandes de la demande
     */
    List<CommandeBO> findByCommandesDemandes_Demande_PkDemandesOrderByDateCreationDesc(Integer demandeId);

}

package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

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

    @Query("select c from CommandeBO c " +
            "where c.pkCommandes in :ids " +
            "and not exists (" +
            "  select 1 from CommandeDemandeBO cd where cd.commande = c" +
            ")")
    List<CommandeBO> findOrphanCommandes(@Param("ids") Set<Integer> ids);

    @Query("select distinct c.pkCommandes from CommandeDemandeBO cd " +
            "join cd.commande c " +
            "where c.pkCommandes in :ids " +
            "and cd.demande is not null " +
            "and cd.demande.pkDemandes not in :purgedIds")
    Set<Integer> findCommandesStillReferenced(@Param("ids") Set<Integer> ids,
            @Param("purgedIds") Set<Integer> purgedIds);

}

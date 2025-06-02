package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Classe de repo pour les CommandeDemandes.
 */
// On désactive la règle de Sonar sur le nommage des méthodes, car pour construire des requêtes on est obligé de mettre des '_'
@SuppressWarnings("java:S100")
public interface CommandeDemandeRepository extends JpaRepository<CommandeDemandeBO, Long> {

    List<CommandeDemandeBO> findByDemande_PkDemandes(Integer pkDemande);

    List<CommandeDemandeBO> findByDemande_PkDemandesOrderByCommande_DateCreationDesc(Integer pkDemande);

    List<CommandeDemandeBO> findByCommande_PkCommandes(Integer pkCommande);

    List<CommandeDemandeBO> findAllByDemande_DernierStatut_LibelleInAndDemande_DernierStatut_DateLessThan(
            List<String> statuts, Date date);


    @Query("""
    SELECT cd FROM CommandeDemandeBO cd
    JOIN cd.commande c
    WHERE cd.demande.pkDemandes = :demandeId
    AND c.dateCreation = (
        SELECT MAX(c2.dateCreation)
        FROM CommandeDemandeBO cd2
        JOIN cd2.commande c2
        WHERE cd2.demande.pkDemandes = :demandeId
    )
""")
    Optional<CommandeDemandeBO> findLatestCommandeForDemande(@Param("demandeId") Integer demandeId);

}

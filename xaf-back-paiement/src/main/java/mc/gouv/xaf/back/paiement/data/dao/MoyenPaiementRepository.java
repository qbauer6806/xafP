package mc.gouv.xaf.back.paiement.data.dao;

import java.util.Collection;
import java.util.List;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// On désactive la règle de Sonar sur le nommage des méthodes, car pour construire des requêtes on est obligé de mettre des '_'
@SuppressWarnings("java:S100")
public interface MoyenPaiementRepository extends JpaRepository<MoyenPaiementBO, String> {

    MoyenPaiementBO findByCommande_PkCommandes(Integer commandId);

    @Query("SELECT mp FROM MoyenPaiementBO mp " +
            "JOIN mp.commande c " +
            "JOIN CommandeDemandeBO cd ON cd.commande.pkCommandes = c.pkCommandes " +
            "WHERE cd.demande.pkDemandes = :demandeId")
    MoyenPaiementBO findByDemande_PkDemandes(Integer demandeId);

    @Query("SELECT mp FROM MoyenPaiementBO mp " +
            "JOIN mp.commande c " +
            "JOIN CommandeDemandeBO cd ON cd.commande.pkCommandes = c.pkCommandes " +
            "WHERE cd.demande.pkDemandes = :demandeId " +
            "AND c.dateCreation = (" +
            "SELECT MAX(c2.dateCreation) FROM CommandeBO c2 " +
            "JOIN CommandeDemandeBO cd2 ON cd2.commande.pkCommandes = c2.pkCommandes " +
            "WHERE cd2.demande.pkDemandes = :demandeId)")
    MoyenPaiementBO findByDemande_PkDemandesAndLastCreationDate(Integer demandeId);

    void deleteByCommande_PkCommandesIn(Collection<Integer> commandeIds);

    /**
     * Récupère le premier {@link MoyenPaiementBO} associé à l'identifiant de demande spécifié qui possède un jeton de
     * moyen de paiement non nul et non vide, trié par date de création par ordre décroissant.
     *
     * @param demandeId
     *         l'identifiant unique de la demande pour laquelle le moyen de paiement est recherché.
     * @param pageable
     *         pour limiter le résultat dans le cas ou ont fourni plusieurs moyens de paiement.
     * @return le {@link MoyenPaiementBO} le plus récent ayant un jeton de moyen de paiement valide pour le demandeId
     *         fourni, ou {@code null} si aucune entité de ce type n'existe.
     */
    @Query("""
                SELECT mp FROM MoyenPaiementBO mp
                JOIN mp.commande c
                JOIN CommandeDemandeBO cd ON cd.commande.pkCommandes = c.pkCommandes
                WHERE cd.demande.pkDemandes = :demandeId
                AND mp.paymentMethodToken IS NOT NULL
                AND mp.paymentMethodToken <> ''
                ORDER BY mp.dateCreation DESC
            """)
    List<MoyenPaiementBO> findFirstByDemandeIdWithToken(@Param("demandeId") Integer demandeId, Pageable pageable);
}

package mc.gouv.xaf.back.paiement.data.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import org.springframework.data.jpa.repository.Query;

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
}

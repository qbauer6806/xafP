package mc.gouv.xaf.back.paiement.data.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;

// On désactive la règle de Sonar sur le nommage des méthodes, car pour construire des requêtes on est obligé de mettre des '_'
@SuppressWarnings("java:S100")
public interface MoyenPaiementRepository extends JpaRepository<MoyenPaiementBO, String> {

    MoyenPaiementBO findByCommande_PkCommandes(Integer commandId);
}

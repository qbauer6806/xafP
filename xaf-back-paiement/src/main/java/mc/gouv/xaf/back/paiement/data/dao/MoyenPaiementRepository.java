package mc.gouv.xaf.back.paiement.data.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;

public interface MoyenPaiementRepository extends JpaRepository<MoyenPaiementBO, String> {

    MoyenPaiementBO findByCommande_PkCommandes(Integer commandId);
}

package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MoyenPaiementRepository extends JpaRepository<MoyenPaiementBO, String> {

    List<MoyenPaiementBO> findByCommande_PkCommande(Integer commandId);
}

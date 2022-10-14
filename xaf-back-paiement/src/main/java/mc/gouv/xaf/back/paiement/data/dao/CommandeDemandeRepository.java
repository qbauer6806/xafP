package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommandeDemandeRepository extends JpaRepository<CommandeDemandeBO, Long> {

    List<CommandeDemandeBO> findByDemande_PkDemandes(Integer pkDemande);

    List<CommandeDemandeBO> findByCommande_PkCommandes(Integer pkCommande);
}

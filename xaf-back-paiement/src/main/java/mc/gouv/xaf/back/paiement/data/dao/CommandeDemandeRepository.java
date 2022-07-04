package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommandeDemandeRepository extends CrudRepository<CommandeDemandeBO, Long> {

    List<CommandeDemandeBO> findByDemande_PkDemandes(Integer pkDemande);
    List<CommandeDemandeBO> findByCommande_PkCommande(Integer pkCommande);
}

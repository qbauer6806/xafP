package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.PaiementHistoriqueBO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author mboutelier.ext
 */
public interface PaiementHistoriqueRepository extends CrudRepository<PaiementHistoriqueBO, Integer> {

    List<PaiementHistoriqueBO> findByFkDemandePkDemandesOrderByDateDesc(Integer pkDemandes);

}

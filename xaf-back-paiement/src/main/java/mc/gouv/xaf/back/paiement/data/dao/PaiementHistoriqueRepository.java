package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.PaiementHistoriqueBO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * @author mboutelier.ext
 */
public interface PaiementHistoriqueRepository extends JpaRepository<PaiementHistoriqueBO, Integer> {

    List<PaiementHistoriqueBO> findByFkDemandesPkDemandesOrderByDateDesc(Integer pkDemandes);
    PaiementHistoriqueBO findByFkDemandesPkDemandesAndStatut(Integer pkDemandes, String statut);

    List<PaiementHistoriqueBO> findByFkDemandes_PkDemandesIn(Collection<Integer> pkDemandes);

    void deleteByFkDemandes_PkDemandesIn(Collection<Integer> pkDemandes);

}

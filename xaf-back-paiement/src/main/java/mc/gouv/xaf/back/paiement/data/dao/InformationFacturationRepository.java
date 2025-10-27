package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.InformationFacturationBO;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;

public interface InformationFacturationRepository extends JpaRepository<InformationFacturationBO, Integer> {

    InformationFacturationBO findByCommande_PkCommandes(Integer commandId);

    void deleteByCommande_PkCommandesIn(Collection<Integer> commandeIds);

}

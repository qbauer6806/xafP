package mc.gouv.xaf.back.stc.data.dao;

import mc.gouv.xaf.back.stc.data.entity.MoyenPaiementBO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MoyenPaiementRepository extends CrudRepository<MoyenPaiementBO, String> {

    List<MoyenPaiementBO> findByCommande_PkCommande(Integer commandId);
}

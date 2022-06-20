package mc.gouv.xaf.back.stc.data.dao;

import mc.gouv.xaf.back.stc.data.entity.MoyenPaiementBO;
import org.springframework.data.repository.CrudRepository;

public interface MoyenPaiementRepository extends CrudRepository<MoyenPaiementBO, String> {

    MoyenPaiementBO findByCommande_Id(Long commandId);
}

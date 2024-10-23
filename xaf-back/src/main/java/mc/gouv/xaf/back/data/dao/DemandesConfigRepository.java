package mc.gouv.xaf.back.data.dao;

import java.util.List;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import org.springframework.data.repository.CrudRepository;

public interface DemandesConfigRepository extends CrudRepository<DemandeConfigBO, String> {

    DemandeConfigBO findOneByBuildId(String buildId);

    DemandeConfigBO findFirstByOrderByBuildIdDesc();

    List<DemandeConfigBO> findAllByOrderByBuildIdDesc();

}

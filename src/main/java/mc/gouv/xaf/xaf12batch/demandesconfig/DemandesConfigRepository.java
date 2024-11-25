package mc.gouv.xaf.xaf12batch.demandesconfig;

import java.util.List;
import mc.gouv.xaf.xaf12batch.dto.DemandeConfigBO;
import org.springframework.data.repository.CrudRepository;

public interface DemandesConfigRepository extends CrudRepository<DemandeConfigBO, String> {

    List<DemandeConfigBO> findAllByOrderByBuildIdDesc();

}

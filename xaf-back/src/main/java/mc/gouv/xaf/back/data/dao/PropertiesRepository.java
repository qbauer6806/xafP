package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.PropertiesBO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author mboutelier.ext
 */
public interface PropertiesRepository extends CrudRepository<PropertiesBO, Integer> {

    List<PropertiesBO> findByDemarchePkDemarches(String demarcheId);

    List<PropertiesBO> findByDemarchePkDemarchesAndType(String demarcheId, String type);

}

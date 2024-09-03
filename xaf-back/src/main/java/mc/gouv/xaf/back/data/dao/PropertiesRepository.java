package mc.gouv.xaf.back.data.dao;

import java.util.List;
import java.util.Optional;
import mc.gouv.xaf.back.data.entity.PropertiesBO;
import org.springframework.data.repository.CrudRepository;

/**
 * @author mboutelier.ext
 */
public interface PropertiesRepository extends CrudRepository<PropertiesBO, Integer> {

    List<PropertiesBO> findAll();

    List<PropertiesBO> findByType(String type);

    Optional<PropertiesBO> findByKey(String key);

    List<PropertiesBO> findByTypeIn(List<String> types);

}

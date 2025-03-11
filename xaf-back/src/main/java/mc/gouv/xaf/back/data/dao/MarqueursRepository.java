package mc.gouv.xaf.back.data.dao;

import java.util.List;
import mc.gouv.xaf.back.data.entity.MarqueurBO;
import org.springframework.data.repository.CrudRepository;

public interface MarqueursRepository extends CrudRepository<MarqueurBO, Integer> {

    List<MarqueurBO> findAllByBuildId(String buildId);

    List<MarqueurBO> findAllByBuildIdAndIdentifiant(String buildId, String identifiant);

}

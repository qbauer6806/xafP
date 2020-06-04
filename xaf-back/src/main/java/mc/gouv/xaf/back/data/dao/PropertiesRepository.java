package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.PropertiesBO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * @author mboutelier.ext
 */
public interface PropertiesRepository extends CrudRepository<PropertiesBO, Integer> {

    List<PropertiesBO> findByDemarchePkDemarches(String demarcheId);

    List<PropertiesBO> findByDemarchePkDemarchesAndType(String demarcheId, String type);

    Optional<PropertiesBO> findByDemarchePkDemarchesAndKey(String demarcheId, String type);

    @Query("select p from PropertiesBO p where p.demarche.pkDemarches = :demarcheId and p.type in :types")
    List<PropertiesBO> findAllInListOfTypes(@Param("demarcheId") String demarcheId, @Param("types") List<String> types);

}

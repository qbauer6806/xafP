package mc.gouv.xaf.back.data.dao;

import java.util.List;
import java.util.stream.Stream;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesDataBO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * @author qdeme
 *
 */
public interface DemandesDataRepository extends CrudRepository<DemandesDataBO, Integer> {

    DemandesDataBO findByFkDemandesPkDemandesAndKey(Integer fkDemandes, String key);

    List<DemandesDataBO> findByFkDemandesPkDemandes(Integer fkDemandes);

    List<DemandesDataBO> findByKeyAndValue(String key, String value);

    List<DemandesDataBO> findByKeyAndValueAndFkDemandesIn(String key, String value, List<DemandeBO> fkDemandes);

    List<DemandesDataBO> findByFkDemandesPkDemandesAndKeyStartsWith(Integer fkDemandes, String key);

    @Query("SELECT d FROM DemandesDataBO d")
    Stream<DemandesDataBO> streamAll();

}

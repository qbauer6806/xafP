package mc.gouv.xaf.back.data.dao;

import java.util.List;
import java.util.Optional;
import mc.gouv.xaf.back.data.entity.MotifBO;
import org.springframework.data.repository.CrudRepository;

/**
 * @author qdeme
 */
public interface MotifsRepository extends CrudRepository<MotifBO, Integer> {

    Optional<MotifBO> findByPkMotifs(Integer pkMotifs);

    List<MotifBO> findAll();

    List<MotifBO> findByStatut(String statut);

    List<MotifBO> findByLangueAndDateArchiveIsNull(String langue);

    Optional<MotifBO> findByCodeAndLangue(String code, String langue);

    List<MotifBO> findByLangueAndStatutAndDateArchiveIsNull(String langue, String statut);

    List<MotifBO> findByLangueAndCodeInAndDateArchiveIsNull(String langue, List<String> codes);

    Optional<MotifBO> findByCodeAndLangueAndStatut(String code, String langue, String statut);

}

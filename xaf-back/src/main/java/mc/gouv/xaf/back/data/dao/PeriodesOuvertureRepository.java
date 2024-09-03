package mc.gouv.xaf.back.data.dao;

import java.util.Date;
import java.util.List;
import mc.gouv.xaf.back.data.entity.PeriodesOuvertureBO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

/**
 * @author qdeme
 */
public interface PeriodesOuvertureRepository extends CrudRepository<PeriodesOuvertureBO, Integer> {

    List<PeriodesOuvertureBO> findAll();

    Page<PeriodesOuvertureBO> findAll(Pageable pageable);

    List<PeriodesOuvertureBO> findByDateFinBeforeOrderByDateFinDesc(Date date);

    List<PeriodesOuvertureBO> findByDateDebutAfter(Date date);

    List<PeriodesOuvertureBO> findByDateDebutLessThanEqualAndDateFinGreaterThanEqual(Date date1, Date date2);

}

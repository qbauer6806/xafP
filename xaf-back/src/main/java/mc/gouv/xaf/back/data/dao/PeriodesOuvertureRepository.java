package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.PeriodesOuvertureBO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * @author qdeme
 */
public interface PeriodesOuvertureRepository extends CrudRepository<PeriodesOuvertureBO, Integer> {

    List<PeriodesOuvertureBO> findByDemarchePkDemarches(String demarcheId);

    Page<PeriodesOuvertureBO> findByDemarchePkDemarches(String demarcheId, Pageable pageable);

    @Query("select p from PeriodesOuvertureBO p " +
            "where p.dateFin < :date and p.demarche.pkDemarches = :demarcheId " +
            "order by p.dateFin desc")
    List<PeriodesOuvertureBO> findAllWithDateFinBeforeDate(@Param("date") Date date, @Param("demarcheId") String demarcheId);

    @Query("select p from PeriodesOuvertureBO p where p.dateDebut > :date and p.demarche.pkDemarches = :demarcheId")
    List<PeriodesOuvertureBO> findAllWithDateDebutAfterDate(@Param("date") Date date, @Param("demarcheId") String demarcheId);

    @Query("select p from PeriodesOuvertureBO p where p.dateDebut <= :date and p.dateFin >= :date and p.demarche.pkDemarches = :demarcheId")
    List<PeriodesOuvertureBO> findAllWithDateDebutAndDateFinBetweenDate(@Param("date") Date date, @Param("demarcheId") String demarcheId);

}

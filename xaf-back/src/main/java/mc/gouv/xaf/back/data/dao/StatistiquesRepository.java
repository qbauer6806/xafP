package mc.gouv.xaf.back.data.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.StatistiqueBO;

public interface StatistiquesRepository extends CrudRepository<StatistiqueBO, Integer> {

    List<StatistiqueBO> findByDemandeIdAndDemarcheId(Integer demandeId, String demarcheId);

    List<StatistiqueBO> findByStatutPublicAndDateBetween(String statut, Date d1, Date d2);

    StatistiqueBO findFirstByDemandeIdAndStatutPublicNotOrderByDateDesc(Integer demandeId, String statut);

    @Query("SELECT d.identifiantDemande,t.statutPublic,t.date,d.date"
            + " FROM StatistiqueBO d, StatistiqueEtatsFinauxBO t WHERE d.demandeId=t.demandeId "
            + " AND d.date between :startDate AND :endDate "
            + " AND d.statutPublic = 'SUPPRIMEE'")
    List<Object> findAllBetweenDates(Date startDate, Date endDate);

}

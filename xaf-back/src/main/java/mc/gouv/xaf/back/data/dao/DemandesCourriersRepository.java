package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.DemandesCourriersBO;

/**
 * @author qdeme
 *
 */
public interface DemandesCourriersRepository extends CrudRepository<DemandesCourriersBO, Integer> {

    List<DemandesCourriersBO> findAll();
    
    @Query("select count(demFile) from DemandesCourriersBO demFile where demFile.url = :url")
    public Integer findHowManyTimeIsFileReferenced(String url);
}

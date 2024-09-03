package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.MotifBO;

/**
 * 
 * @author qdeme
 *
 */
public interface MotifsRepository extends CrudRepository<MotifBO, Integer> {
    
    MotifBO findByPkMotifs(Integer pkMotifs);

    List<MotifBO> findAll();

    List<MotifBO> findByStatut(String statut);
    
}

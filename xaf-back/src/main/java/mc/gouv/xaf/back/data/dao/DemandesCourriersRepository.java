package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.DemandesCourriersBO;

/**
 * @author qdeme
 */
public interface DemandesCourriersRepository extends CrudRepository<DemandesCourriersBO, Integer> {

    List<DemandesCourriersBO> findAll();

    Integer countByUrl(String url);
}

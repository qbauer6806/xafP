package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.DemandesFilesBO;

/**
 * @author qdeme
 *
 */
public interface DemandesFilesRepository extends CrudRepository<DemandesFilesBO, Integer> {
	
    List<DemandesFilesBO> findAllByUrl(String url);
}

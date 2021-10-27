package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.DemandesComplementsFilesBO;
import mc.gouv.xaf.back.data.entity.DemandesFilesBO;

/**
 * @author qdeme
 *
 */
public interface DemandesComplementsFilesRepository extends CrudRepository<DemandesComplementsFilesBO, Integer> {

	List<DemandesComplementsFilesBO> findAllByUrl(String url);
}

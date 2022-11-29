package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.DemandesComplementsFilesBO;

/**
 * @author qdeme
 *
 */
public interface DemandesComplementsFilesRepository extends CrudRepository<DemandesComplementsFilesBO, Integer> {

	List<DemandesComplementsFilesBO> findAllByUrl(String url);
}

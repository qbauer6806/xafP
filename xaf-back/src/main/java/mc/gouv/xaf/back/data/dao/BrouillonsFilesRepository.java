package mc.gouv.xaf.back.data.dao;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.BrouillonsFilesBO;

import java.util.List;

/**
 * @author qdeme
 *
 */
public interface BrouillonsFilesRepository extends CrudRepository<BrouillonsFilesBO, Integer> {

    List<BrouillonsFilesBO> findAllByUrl(String url);

}

package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.DemandesComplementsFilesBO;

/**
 * @author qdeme
 *
 */
public interface DemandesComplementsFilesRepository extends CrudRepository<DemandesComplementsFilesBO, Integer> {

	List<DemandesComplementsFilesBO> findAllByUrl(String url);

    @Query("select count(demFile) from DemandesComplementsFilesBO demFile where demFile.url = :url")
    public Integer findHowManyTimeIsFileReferenced(String url);
}

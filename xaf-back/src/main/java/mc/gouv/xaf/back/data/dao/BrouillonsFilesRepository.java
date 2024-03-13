package mc.gouv.xaf.back.data.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.BrouillonsFilesBO;

/**
 * @author qdeme
 *
 */
public interface BrouillonsFilesRepository extends CrudRepository<BrouillonsFilesBO, Integer> {

    @Query("select count(demFile) from BrouillonsFilesBO demFile where demFile.url = :url")
    public Integer findHowManyTimeIsFileReferenced(String url);

}

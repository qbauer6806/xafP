package mc.gouv.xaf.back.data.dao;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.BrouillonsFilesBO;

import java.util.List;

/**
 * @author qdeme
 *
 */
public interface BrouillonsFilesRepository extends CrudRepository<BrouillonsFilesBO, Integer> {

    @Query("select count(demFile) from BrouillonsFilesBO demFile where demFile.url = :url")
    public Integer findHowManyTimeIsFileReferenced(String url);

    List<BrouillonsFilesBO> findAllByUrl(String url);

    @Modifying
    @Query("delete from BrouillonsFilesBO BFBO where BFBO.fkBrouillons.pkBrouillons in (select BBO.pkBrouillons from BrouillonBO BBO where BBO.buildId != :buildIdCourant )")
    void deleteBrouillonsFilesWithBuildIdOtherThan(String buildIdCourant);
}

package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.BrouillonsFilesBO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * @author qdeme
 */
public interface BrouillonsFilesRepository extends CrudRepository<BrouillonsFilesBO, Integer> {

    Integer countByUrl(String url);

    @Modifying
    @Query("delete from BrouillonsFilesBO BFBO where BFBO.fkBrouillons.pkBrouillons in (select BBO.pkBrouillons from BrouillonBO BBO where BBO.config.buildId != :buildIdCourant )")
    void deleteBrouillonsFilesWithBuildIdOtherThan(String buildIdCourant);
}

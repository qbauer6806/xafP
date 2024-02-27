package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.DemandesFilesBO;

/**
 * @author qdeme
 *
 */
public interface DemandesFilesRepository extends CrudRepository<DemandesFilesBO, Integer> {
	
    public List<DemandesFilesBO> findAllByUrl(String url);

    @Query("select count(demFile) from DemandesFilesBO demFile inner join demFile.fkDemandes dem where demFile.url = :url and dem.pkDemandes != :pkDemandes")
    public Integer findHowManyTimeIsFileReferenced(String url, Integer pkDemandes);

}

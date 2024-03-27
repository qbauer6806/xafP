package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.DemandesFilesBO;

/**
 * @author qdeme
 *
 */
// On désactive la règle de Sonar sur le nommage des méthodes, car pour construire des requêtes on est obligé de mettre des '_'
@SuppressWarnings("java:S100")
public interface DemandesFilesRepository extends CrudRepository<DemandesFilesBO, Integer> {
	
    List<DemandesFilesBO> findAllByUrl(String url);

    List<DemandesFilesBO> findAllByFkDemandes_PkDemandesAndMeta(Integer pkDemande, String meta);

    @Query("select count(demFile) from DemandesFilesBO demFile where demFile.url = :url")
    public Integer findHowManyTimeIsFileReferenced(String url);

    @Query("select DF from DemandesFilesBO DF where DF.fkDemandes not in (select D.pkDemandes from DemandeBO D)")
    public List<DemandesFilesBO> findAllNonReferencedFiles();

}

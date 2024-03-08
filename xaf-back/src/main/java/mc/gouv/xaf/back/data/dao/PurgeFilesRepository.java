package mc.gouv.xaf.back.data.dao;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.PurgeFilesBO;

/**
 * @author agaidi.ext
 *
 */
public interface PurgeFilesRepository extends CrudRepository<PurgeFilesBO, Integer> {

    @Modifying
    @Query("insert into PurgeFilesBO(url) select DFBO.url from DemandeBO DBO inner join DBO.files DFBO where DBO.pkDemandes = :pkDemandes")
    void insertFilesToPurge(Integer pkDemandes);

    @Modifying
    @Query("insert into PurgeFilesBO(url) select DCFBO.url from DemandeBO DBO inner join DBO.demandesComplements DCBO "
            + " inner join DCBO.files DCFBO where DBO.pkDemandes =:pkDemandes")
    void insertFilesComplementsToPurge(Integer pkDemandes);

    @Modifying
    @Query("insert into PurgeFilesBO(url) select DCBO.url from DemandesCourriersBO DCBO inner join DCBO.fkDemandes DBO "
            + " where DBO.pkDemandes =:pkDemandes")
    void insertFilesCourrierToPurge(Integer pkDemandes);
}

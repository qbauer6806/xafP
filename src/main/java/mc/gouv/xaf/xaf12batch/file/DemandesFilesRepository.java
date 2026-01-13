package mc.gouv.xaf.xaf12batch.file;

import java.util.List;
import mc.gouv.xaf.xaf12batch.dto.DemandesFilesBO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface DemandesFilesRepository extends CrudRepository<DemandesFilesBO, Integer> {
    @Query(value = """
    WITH urls_multi_demandes AS (
        SELECT URL
        FROM DEM_DEMANDES_FILES
        GROUP BY URL
        HAVING COUNT(DISTINCT FK_DEMANDES) > 1
    ),
    ranked_files AS (
        SELECT
            df.PK_DEMANDESFILES
        FROM DEM_DEMANDES_FILES df
        JOIN urls_multi_demandes u ON u.URL = df.URL
        WHERE df.PK_DEMANDESFILES NOT IN (
            SELECT MIN(PK_DEMANDESFILES)
            FROM DEM_DEMANDES_FILES
            GROUP BY URL
        )
        ORDER BY df.PK_DEMANDESFILES
    )
    SELECT PK_DEMANDESFILES
    FROM ranked_files
    """,
            nativeQuery = true)
    List<Integer> findDuplicateFileIdsExcludingFirst();

}

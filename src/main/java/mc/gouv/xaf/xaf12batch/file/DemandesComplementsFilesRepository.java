package mc.gouv.xaf.xaf12batch.file;

import java.util.List;
import mc.gouv.xaf.xaf12batch.dto.DemandesComplementsFilesBO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface DemandesComplementsFilesRepository extends CrudRepository<DemandesComplementsFilesBO, Integer> {

    @Query(value = """
        WITH urls_multi_complements AS (
            SELECT URL
            FROM DEM_DEMANDES_COMPLEMENTS_FILES
            WHERE URL IS NOT NULL
              AND URL <> ''
            GROUP BY URL
            HAVING COUNT(DISTINCT FK_DEMANDESCOMPLEMENTS) > 1
        ),
        ranked_files AS (
            SELECT
                df.PK_DEMANDESCOMPLEMENTSFILES,
                ROW_NUMBER() OVER (
                    PARTITION BY df.URL
                    ORDER BY df.PK_DEMANDESCOMPLEMENTSFILES
                ) AS rn
            FROM DEM_DEMANDES_COMPLEMENTS_FILES df
            JOIN urls_multi_complements u
              ON u.URL = df.URL
        )
        SELECT PK_DEMANDESCOMPLEMENTSFILES
        FROM ranked_files
        WHERE rn > 1
        """,
            nativeQuery = true)
    List<Integer> findDuplicateComplementFileIdsExcludingFirst();

}

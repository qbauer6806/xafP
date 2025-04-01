package mc.gouv.xaf.back.data.dao;

import java.util.List;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.projection.DemandeDataExportProjection;
import mc.gouv.xaf.back.data.entity.DemandesDataBO;
import org.springframework.data.repository.CrudRepository;

/**
 * @author qdeme
 */
public interface DemandesDataRepository extends CrudRepository<DemandesDataBO, Integer> {

    DemandesDataBO findByFkDemandesPkDemandesAndKey(Integer fkDemandes, String key);

    List<DemandesDataBO> findByFkDemandesPkDemandes(Integer fkDemandes);

    List<DemandesDataBO> findByKeyAndValue(String key, String value);

    List<DemandesDataBO> findByKeyAndValueAndFkDemandesIn(String key, String value, List<DemandeBO> fkDemandes);

    List<DemandeDataExportProjection> findByFkDemandes_PkDemandes(Integer pkDemandes);
}

package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesDataBO;

/**
 * @author qdeme
 *
 */
public interface DemandesDataRepository extends CrudRepository<DemandesDataBO, Integer> {

    public DemandesDataBO findByFkDemandesPkDemandesAndKey(Integer fkDemandes, String key);

    public List<DemandesDataBO> findByFkDemandesPkDemandes(Integer fkDemandes);

    public List<DemandesDataBO> findByKeyAndValue(String key, String value);

    public List<DemandesDataBO> findByKeyAndValueAndFkDemandesIn(String key, String value, List<DemandeBO> fkDemandes);

    public List<DemandesDataBO> findByFkDemandesPkDemandesAndKeyStartsWith(Integer fkDemandes, String key);

}

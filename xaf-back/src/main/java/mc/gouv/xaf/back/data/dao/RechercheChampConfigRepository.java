package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.RechercheChampConfigBO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RechercheChampConfigRepository extends CrudRepository<RechercheChampConfigBO, Integer> {

    RechercheChampConfigBO findByCle(String cle);

    List<RechercheChampConfigBO> findByCategorieId(Integer id);

}

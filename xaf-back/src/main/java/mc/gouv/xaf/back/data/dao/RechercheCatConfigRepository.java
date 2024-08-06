package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.RechercheCatConfigBO;
import org.springframework.data.repository.CrudRepository;

public interface RechercheCatConfigRepository extends CrudRepository<RechercheCatConfigBO, Integer> {

    RechercheCatConfigBO findByLibelle(String libelle);

}

package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.RechercheCatConfigBO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RechercheCatConfigRepository extends JpaRepository<RechercheCatConfigBO, Integer> {

    RechercheCatConfigBO findByLibelle(String libelle);

}

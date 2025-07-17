package mc.gouv.xaf.back.data.dao;

import java.util.List;
import mc.gouv.xaf.back.data.entity.RechercheChampConfigBO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RechercheChampConfigRepository extends JpaRepository<RechercheChampConfigBO, Integer> {

    RechercheChampConfigBO findByCle(String cle);

    List<RechercheChampConfigBO> findByCategorieId(Integer id);

    boolean existsByCle(String cle);
}

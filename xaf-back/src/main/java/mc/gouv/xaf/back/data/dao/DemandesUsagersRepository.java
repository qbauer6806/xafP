package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.DemandesUsagersBO;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author uek
 */
public interface DemandesUsagersRepository extends JpaRepository<DemandesUsagersBO, Integer> {

    DemandesUsagersBO findOneById(Integer usagerId);

}

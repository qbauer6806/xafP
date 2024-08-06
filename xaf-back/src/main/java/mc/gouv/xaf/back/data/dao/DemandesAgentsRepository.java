package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.DemandesAgentsBO;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author uek
 *
 */
public interface DemandesAgentsRepository extends JpaRepository<DemandesAgentsBO, Integer> {

    
}

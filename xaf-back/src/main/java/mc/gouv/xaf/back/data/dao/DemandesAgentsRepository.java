package mc.gouv.xaf.back.data.dao;

import java.util.Optional;
import mc.gouv.xaf.back.data.entity.DemandesAgentsBO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemandesAgentsRepository extends JpaRepository<DemandesAgentsBO, Integer> {

    Optional<DemandesAgentsBO> findById(String id);

}

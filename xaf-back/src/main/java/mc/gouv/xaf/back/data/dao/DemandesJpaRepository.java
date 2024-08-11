package mc.gouv.xaf.back.data.dao;

import java.util.stream.Stream;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DemandesJpaRepository extends JpaRepository<DemandeBO, Integer> {

    @Query("SELECT d FROM DemandeBO d")
    Stream<DemandeBO> streamAll();

}

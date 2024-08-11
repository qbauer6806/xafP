package mc.gouv.xaf.back.data.dao;

import java.util.List;
import java.util.stream.Stream;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesFilesBO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface DemandesJpaFilesRepository extends CrudRepository<DemandesFilesBO, Integer> {

    @Query("SELECT d FROM DemandesFilesBO d")
    Stream<DemandesFilesBO> streamAll();

}

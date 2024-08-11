package mc.gouv.xaf.back.data.dao;

import java.util.stream.Stream;
import mc.gouv.xaf.back.data.entity.DemandesComplementsFilesBO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface DemandesJpaComplementsFilesRepository extends CrudRepository<DemandesComplementsFilesBO, Integer> {

    @Query("SELECT d FROM DemandesComplementsFilesBO d")
    Stream<DemandesComplementsFilesBO> streamAll();

}

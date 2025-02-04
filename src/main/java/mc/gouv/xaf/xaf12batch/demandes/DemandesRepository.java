package mc.gouv.xaf.xaf12batch.demandes;

import mc.gouv.xaf.xaf12batch.dto.DemandeBO;
import org.springframework.data.repository.CrudRepository;

public interface DemandesRepository extends CrudRepository<DemandeBO, Integer> {


}

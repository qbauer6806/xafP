package mc.gouv.xaf.xaf12batch.bpm;

import org.springframework.data.repository.CrudRepository;
import mc.gouv.xaf.xaf12batch.dto.DemandeBO;

public interface DemandesCommentaireRepository extends CrudRepository<DemandesCommentaireBO, Integer> {

    boolean existsByFkDemandes(DemandeBO demandeBO);

}

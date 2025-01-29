package mc.gouv.xaf.back.data.dao;

import java.util.List;
import mc.gouv.xaf.back.data.entity.DemandesCommentaireBO;
import org.springframework.data.repository.CrudRepository;

public interface DemandesCommentaireRepository extends CrudRepository<DemandesCommentaireBO, Integer> {

    List<DemandesCommentaireBO> findByFkDemandesPkDemandes(Integer pkDemandes);

}

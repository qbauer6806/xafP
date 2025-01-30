package mc.gouv.xaf.back.data.dao;

import java.util.List;
import mc.gouv.xaf.back.data.entity.DemandesCommentaireBO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface DemandesCommentaireRepository extends CrudRepository<DemandesCommentaireBO, Integer> {

    List<DemandesCommentaireBO> findByFkDemandesPkDemandesOrderByDateAsc(Integer pkDemandes);

    @Modifying
    @Transactional
    void deleteByFkDemandesPkDemandes(Integer pkDemandes);
}

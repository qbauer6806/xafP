package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommandeRepository extends JpaRepository<CommandeBO, Long> {

    /**
     * Récupère la likste des commandes liées à une demande
     *
     * @param demandeId, la clé primaire de la demande
     * @return une liste contenant les commandes de la demande
     */
    List<CommandeBO> findByCommandesDemandes_Demande_PkDemandesOrderByDateCreationDesc(Integer demandeId);

}

package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.back.data.entity.StatistiqueBO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PurgeDemandeDTO;
import mc.gouv.xaf.shared.dto.StatistiqueDTO;

import java.util.List;

/**
 * Service permettant la manipulation des demandes purgées.
 */
public interface PurgeService {

    /**
     * Récupère toutes les demandes ayant comme statut "SUPPRIMEE"
     * @return statistiques
     */
    List<PurgeDemandeDTO> getDemandesPurgees();
}

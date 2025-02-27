package mc.gouv.xaf.back.service.data;

import java.util.List;
import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 * Service permettant de gérer les usagers.
 *
 * @author qdeme
 */
public interface UsagersService {

    /**
     * Permet de gérer les demandes et les accès suite à la désinscription d'un usager
     */
    void desinscriptionUsager(Integer usagerId, String statutAnnulation, String codeMotif);

    void desinscriptionUsager(Integer usagerId, String statutAnnulation, String codeMotif,
            List<DemandeDTO> demandesAPasserEnAnnuleeDTO);

    Integer getNbDemandesUsager(Integer usagerId);

}

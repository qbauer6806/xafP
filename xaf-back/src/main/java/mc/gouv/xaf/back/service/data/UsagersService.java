package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;

/**
 * Service permettant de gérer les usagers.
 *
 * @author qdeme
 */
public interface UsagersService {

    /**
     * Permet de gérer les demandes et les accès suite à la désinscription d'un usager
     */
	void desinscriptionUsager(Integer usagerId, StatutPublicOuInterneDTO statutAnnulation, String codeMotif);

    Integer getNbDemandesUsager(Integer usagerId);

}

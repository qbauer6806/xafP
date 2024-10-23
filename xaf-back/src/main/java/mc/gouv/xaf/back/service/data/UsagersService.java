package mc.gouv.xaf.back.service.data;

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

    Integer getNbDemandesUsager(Integer usagerId);

}

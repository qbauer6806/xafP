package mc.gouv.xaf.back.service.data;

import java.util.List;

/**
 * Service permettant de gérer les usagers.
 * 
 * @author qdeme
 *
 */
public interface UsagersService {

	/**
	 * Permet de gérer les demandes et les accès suite à la désinscription d'un usager
	 * 
	 * @param demarcheId
	 * @param usagerId
	 * @param statutsFinaux
	 * @param statutAnnulation
	 * @param codeMotif
	 */
    public void desinscriptionUsager(String demarcheId, Integer usagerId,
            List<String> statutsFinaux, String statutAnnulation, String codeMotif);

	public Integer getNbDemandesUsager(String demarcheId, Integer usagerId);
    
}

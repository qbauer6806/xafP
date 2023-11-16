package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.shared.dto.RestitutionStatistiquesDTO;
import mc.gouv.xaf.shared.dto.StatistiqueDTO;

/**
 * Service permettant la manipulation des statistiques liés à la restitutions.
 */
public interface RestitutionStatistiquesService {

    /**
     * @param statistiqueId id à supprimer
     */
    void deleteRestitutionStatistique(Integer statistiqueId);


    /**
     * @param stat objet à sauvegarder
     * @return objet sauvegardé
     */
    StatistiqueDTO saveRestitutionStatistique(RestitutionStatistiquesDTO restitutionStat);


	/**
	 * Supprimer les statistiques liées à un usager
	 */
	void deleteRestitutionStatistiques(Integer usagerId);

}

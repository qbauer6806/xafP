package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.back.data.entity.StatistiqueBO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.StatistiqueDTO;

import java.util.List;

/**
 * Service permettant la manipulation des statistiques.
 */
public interface StatistiquesService {

    /**
     * @param statistiqueId id à supprimer
     */
    void deleteStatistique(Integer statistiqueId);


    /**
     * @param stat objet à sauvegarder
     * @return objet sauvegardé
     */
    StatistiqueDTO saveStatistique(StatistiqueDTO stat);

    /**
     * @param demandeDTO objet contenant l'information
     * @return objet sauvegardé
     */
    StatistiqueDTO saveStatistique(DemandeDTO demandeDTO);

	/**
	 * Supprimer les statistiques liées à une demande
	 * @param demarcheId
	 * @param pkDemande
	 */
	void deleteStatistiques(String demarcheId, Integer pkDemande);

}

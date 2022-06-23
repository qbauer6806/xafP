package mc.gouv.xaf.back.service.data;

import java.util.List;

import mc.gouv.xaf.shared.dto.StatistiquesTypesDTO;

/**
 * Service permettant la manipulation des statistiques types.
 */
public interface StatistiquesTypesService {

	
	/**
	 * Suppression de tous les types pour un identifiant donné
	 * @param identifiantDemande
	 */
	void deleteStatistiquesTypes(String identifiantDemande);
	
	/**
	 * Sauvegarde du type de demande courant dans la DB
	 * @param statType
	 * @return Le type crée
	 */
	StatistiquesTypesDTO saveStatistiquesTypes(StatistiquesTypesDTO statType);
	
	/**
	 * Recupère les statistiques type pour la demande donnée
	 * @param identifiantDemande
	 * @return
	 */
	List<StatistiquesTypesDTO> getStatistiquesTypes(String identifiantDemande);
}

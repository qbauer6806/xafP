package mc.gouv.xaf.back.service.purge;

import java.util.List;

import mc.gouv.xaf.shared.dto.PurgeDemandeDTO;

public interface PurgeDemandesService {

	void purgerDemandesDansStatuts(List<String> statuts, int jours) throws Exception;

	/**
	 * Récupère toutes les demandes ayant comme statut "SUPPRIMEE"
	 * @return statistiques
	 */
	List<PurgeDemandeDTO> getDemandesPurgees();
}

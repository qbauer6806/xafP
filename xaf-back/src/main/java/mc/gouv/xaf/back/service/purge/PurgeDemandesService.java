package mc.gouv.xaf.back.service.purge;

import com.fasterxml.jackson.core.JsonProcessingException;
import mc.gouv.xaf.shared.dto.PurgeDemandeDTO;

import java.util.List;

public interface PurgeDemandesService {

	void purgerDemandesDansStatuts(List<String> statuts, int jours) throws JsonProcessingException;

	/**
	 * Récupère toutes les demandes ayant comme statut "SUPPRIMEE"
	 * @return statistiques
	 */
	List<PurgeDemandeDTO> getDemandesPurgees();
}

package mc.gouv.xaf.back.service.purge;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface PurgeDemandesService {

	String DEMANDES_TRIGGER_NAME = "PurgeDemandesSchedulingTrigger";
	String PAIEMENTS_TRIGGER_NAME = "PurgeDemandesPaiementsSchedulingTrigger";

	void purgerDemandesDansStatuts(List<String> statuts, int jours) throws JsonProcessingException;

	/**
	 * Récupère toutes les demandes ayant comme statut "SUPPRIMEE"
	 * @return statistiques
	 */
    List<Object> getDemandesPurgees();
	
	/**
	 * Spécifie la méthode d'envoi des emails aux agents
	 */
	void envoisMailAgentPurge(String demandesAPurger, String delai);

	/**
	 * Récupère la dernière execution du job de purge
	 */
	Date getDateDerniereExecution();
	
}

package mc.gouv.xaf.back.service;

import java.util.List;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.ExcelRechercheDTO;

/**
 * Permet à un TS d'implémenter une ou plusieurs façons différentes de retourner des demandes à partir d'un critère de date
 * ou de statut (pour l'export Excel par exemple)
 * 
 * Exemple :
 * 		- recherche sur date de création
 * 		- recherche sur date de dernier statut
 * 		- recherche sur date de dernier statut interne "en attente de paiement"
 * 
 * @author qdeme
 *
 */
public interface DemandeExcelRechercheProvider {

	List<DemandeDTO> getDemandes(ExcelRechercheDTO rechercheDto);
	
}

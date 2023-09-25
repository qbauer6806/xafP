package mc.gouv.xaf.back.denjs.service;

import java.util.List;

import mc.gouv.xaf.back.denjs.dto.DenjsAffectationAgentDTO;
import mc.gouv.xaf.back.denjs.dto.DenjsEtablissementDTO;

/**
 * Service permettant de gérer l'affectation des agents ou des demandes à des établissements, dans le cadre
 * des téléservices pour la DENJS
 * 
 * @author qdeme
 *
 */
public interface DenjsAffectationService {

	/**
	 * Récupérer la liste des affectations des agents à des établissements, depuis la base
	 */
	List<DenjsAffectationAgentDTO> getAffectationsAgents();
	
	/**
	 * Récupérer la liste des établissements, depuis la base
	 */
	List<DenjsEtablissementDTO> getEtablissements();
	
	/**
	 * Affecter un agent à un établissement
	 * @param affectation Mettre etablissementCode à null ou "" pour désaffecter
	 */
	List<DenjsAffectationAgentDTO> affecterAgentEtablissement(DenjsAffectationAgentDTO affectation);
	
	/**
	 * Affecter une demande à un établissement
	 * @param pkDemande pkDemande de la demande à affecter
	 * @param etablissementCode Code de l'établissement
	 */
	void affecterDemandeEtablissement(Integer pkDemande, String etablissementCode);
	
	/**
	 * Désaffecter une demande d'un établissement
	 * @param pkDemande pkDemande de la demande à désaffecter
	 */
	void desaffecterDemandeEtablissement(Integer pkDemande);
	
	/**
	 * Récupérer l'établissement auquel est affecté une demande
	 */
	String getAffectationDemandeEtablissement(Integer pkDemande);

	/**
	 * Récupérer un établissement à partir de son code
	 */
	DenjsEtablissementDTO getEtablissementFromCode(String code, List<DenjsEtablissementDTO> etabs);

	/**
	 * Récupère l'affectation d'un agent à un établissement
	 */
	DenjsAffectationAgentDTO getAffectationAgent(String matricule);
	
}

package mc.gouv.xaf.back.service;

import java.util.List;
import java.util.Map;

import mc.gouv.xaf.back.dto.GenericStatusDTO;
import mc.gouv.xaf.back.dto.StatutPublicOuInterneDTO;
import mc.gouv.dem.shared.model.DemandeDTO;

/**
 * 
 * Service implémenté par la démarche permettant de fournir à xaf-back des informations propres à chaque démarche.
 * 
 * @author qdeme
 *
 */
public interface DemarchesDataProvider {

    public String getStatusLibelle(String status);

    public String getStatusColorClass(StatutPublicOuInterneDTO statutPublicOuInterne);

    public String getDemandeur(Object contenuDemandeDTO);

    public List<GenericStatusDTO> getCandidateStatusesForMotifs();

    public StatutPublicOuInterneDTO getStatutPublicOuInterne(DemandeDTO demandeDto);

    public Map<String, String> getStatusMap();

    public Map<String, String> getPrivateStatusMap();

    public String getVersion();

    public StatutPublicOuInterneDTO getStatutPublicOuInterne(Integer pkDemande, String statutLibelle);

	public Map<String, String> getLanguesDisponibles();
	
	public boolean getDemarcheCanGenerateCourriers();

}

package mc.gouv.af.back.service;

import java.util.List;

import mc.gouv.af.back.dto.GenericStatusDTO;
import mc.gouv.af.back.dto.StatutPublicOuInterneDTO;

/**
 * 
 * Service implémenté par la démarche permettant de fournir à af-back des informations propres
 * à chaque démarche.
 * 
 * @author qdeme
 *
 */
public interface DemarchesDataProvider {
    
    public String getStatusLibelle(String status);

    public String getStatusColorClass(StatutPublicOuInterneDTO statutPublicOuInterne);

    public String getDemandeur(Object contenuDemandeDTO);

    public List<GenericStatusDTO> getCandidateStatusesForMotifs();

}

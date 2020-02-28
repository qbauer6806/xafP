package mc.gouv.xaf.back.controller;

import java.util.List;


import com.fasterxml.jackson.core.JsonProcessingException;

import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeInputDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;

/**
 * 
 * Interface spécifiant les méthodes devant être implémentées en tant que Web Services dans les démarches BACK.
 * 
 * @author qdeme
 * @author fgaujous
 *
 */
public interface AfApiController {

    public void annulerDemande(Integer demandeId, Integer usagerId);

    public DemandeComplementsDTO repondreDemandeComplements(Integer demandeId, Integer icId,
            DemandeComplementsReponseDTO reponse) throws Exception;

    public DemandeDTO getDemande(Integer usagerId, Integer demandeId);

    public List<DemandeDTO> getDemandes(Integer usagerId);

    public List<DemandeComplementsDTO> getDemandeComplements(Integer demandeId);

    public DemandeComplementsDTO getDemandeComplements(Integer demandeId, Integer icId);

    public DemandeDTO associerDemandeCourrier(String identifiantDemande, String stringToCheck, Integer usagerId);

    public void desinscriptionUsager(Integer usagerId, String langue);

    public AccessDTO createOrUpdateAccess(Integer usagerId, AccessInputDTO dto);

    public AccessDTO getAccess(Integer usagerId);

    public UsagerCourrierDTO getUsagerCourrier(Integer usagerCourrierId);

    public List<MotifDTO> getMotifs();

    public DemandeDTO creerDemande(DemandeInputDTO demande, Integer usagerId) throws JsonProcessingException;
    
    public List<PeriodeOuvertureDTO> getPeriodesOuverture();

}

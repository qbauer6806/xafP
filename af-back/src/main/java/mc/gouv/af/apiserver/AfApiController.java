package mc.gouv.af.apiserver;

import java.util.List;

import mc.gouv.dem.shared.model.AccessDTO;
import mc.gouv.dem.shared.model.AccessInputDTO;
import mc.gouv.dem.shared.model.DemandeComplementsDTO;
import mc.gouv.dem.shared.model.DemandeComplementsReponseDTO;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.DemandeInputDTO;
import mc.gouv.dem.shared.model.MotifDTO;
import mc.gouv.dem.shared.model.UsagerCourrierDTO;

/**
 * 
 * Interface spécifiant les méthodes devant être implémentées en tant que Web Services
 * dans les démarches BACK.
 * 
 * @author qdeme
 * @author fgaujous
 *
 */
public interface AfApiController {

    public void annulerDemande(Integer demandeId, Integer usagerId);

    public DemandeDTO creerDemande(DemandeInputDTO demande, Integer usagerId);

    public DemandeComplementsDTO repondreDemandeComplements(Integer demandeId, Integer icId,
            DemandeComplementsReponseDTO reponse);

    public DemandeDTO getDemande(Integer usagerId, Integer demandeId);

    public List<DemandeDTO> getDemandes(Integer usagerId);

    public List<DemandeComplementsDTO> getDemandeComplements(Integer demandeId);

    public DemandeComplementsDTO getDemandeComplements(Integer demandeId, Integer icId);
    
    public DemandeDTO associerDemandeCourrier(String identifiantDemande, String stringToCheck, Integer usagerId);
    
    public void desinscriptionUsager(Integer usagerId, String hashedPassword);
    
    public AccessDTO createOrUpdateAccess(Integer usagerId, AccessInputDTO dto);
    
    public AccessDTO getAccess(Integer usagerId);
    
    public UsagerCourrierDTO getUsagerCourrier(Integer usagerCourrierId);
    
    public List<MotifDTO> getMotifs();

}

package mc.gouv.xaf.back.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import mc.gouv.xaf.shared.dto.*;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;

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
    
    @SuppressWarnings("rawtypes")
	public ResponseEntity getCustomRequest(HttpServletRequest request);
    
    @SuppressWarnings("rawtypes")
	public ResponseEntity postCustomRequest(HttpServletRequest request);
    
    @SuppressWarnings("rawtypes")
	public ResponseEntity putCustomRequest(HttpServletRequest request);
    
    @SuppressWarnings("rawtypes")
	public ResponseEntity deleteCustomRequest(HttpServletRequest request);

    List<PropertiesDTO> getFrontProperties();

}

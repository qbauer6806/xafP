package mc.gouv.xaf.back.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.xml.sax.SAXException;
import mc.gouv.xaf.shared.dto.*;
import org.apache.tika.exception.TikaException;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * Interface spécifiant les méthodes devant être implémentées en tant que Web Services dans les démarches BACK.
 *
 * @author qdeme
 * @author fgaujous
 */
public interface AfApiController {

    void annulerDemande(Integer demandeId, Integer usagerId);

    DemandeComplementsDTO repondreDemandeComplements(Integer demandeId, Integer icId,
                                                     DemandeComplementsReponseDTO reponse) throws IOException, TikaException, SAXException;

    DemandeDTO getDemande(Integer usagerId, Integer demandeId);

    List<DemandeDTO> getDemandes(Integer usagerId);

    Page<DemandeDTO> getDemandesPageable(Integer usagerID, PageParamDTO paramDTO);

    List<DemandeComplementsDTO> getDemandeComplements(Integer demandeId);

    DemandeComplementsDTO getDemandeComplements(Integer demandeId, Integer icId);

    DemandeDTO associerDemandeCourrier(String identifiantDemande, String stringToCheck, Integer usagerId);

    /**
     * @param fromGU   true si la désinscription du TS est demandée à cause d'une désinscription depuis le Guichet Unique
     */
    void desinscriptionUsager(Integer usagerId, String langue, boolean fromGU);

    AccessDTO createOrUpdateAccess(Integer usagerId, AccessInputDTO dto);

    AccessDTO getAccess(Integer usagerId);

    UsagerCourrierDTO getUsagerCourrier(Integer usagerCourrierId);

    List<MotifDTO> getMotifs();

    DemandeDTO creerDemande(DemandeInputDTO demande, Integer usagerId) throws JsonProcessingException;
    
    DemandeDTO updateDemande(Integer demandeId, DemandeInputDTO demande, Integer usagerId) throws JsonProcessingException;

    List<PeriodeOuvertureDTO> getPeriodesOuverture();

    JsonNode getDonneesExternes(Integer usagerId);

    @SuppressWarnings("rawtypes")
    ResponseEntity getCustomRequest(HttpServletRequest request);

    @SuppressWarnings("rawtypes")
    ResponseEntity postCustomRequest(HttpServletRequest request);

    @SuppressWarnings("rawtypes")
    ResponseEntity putCustomRequest(HttpServletRequest request);

    @SuppressWarnings("rawtypes")
    ResponseEntity deleteCustomRequest(HttpServletRequest request);

    List<PropertiesDTO> getFrontProperties();

    BrouillonDTO creerBrouillon(BrouillonDTO brouillon, Integer usagerId);

    BrouillonDTO updateBrouillon(BrouillonDTO brouillon, Integer usagerId);

    List<BrouillonDTO> getBrouillons(Integer usagerId);

    Page<BrouillonDTO> getBrouillonsPageable(Integer usagerId, PageParamDTO paramDTO);

    BrouillonDTO getBrouillon(Integer pkBrouillons, Integer usagerId);

    void deleteBrouillon(Integer pkBrouillons, Integer usagerId) throws JsonProcessingException;

}

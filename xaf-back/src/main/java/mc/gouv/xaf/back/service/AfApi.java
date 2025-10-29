package mc.gouv.xaf.back.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.tika.exception.TikaException;
import org.springframework.http.ResponseEntity;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeInputDTO;
import mc.gouv.xaf.shared.dto.DonneesMConnectDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.dto.Page;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import mc.gouv.xaf.shared.dto.PaysDTO;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;

/**
 * 
 * Interface spécifiant les fonctionnalités offertes par l'API pour le AfApiController (appels arrivant du FO).
 * 
 * @author qdeme
 * 
 */
public interface AfApi {

    void annulerDemande(Integer demandeId, Integer usagerId);

    DemandeDTO creerDemande(@Valid DemandeInputDTO demande, Integer usagerId) throws JsonProcessingException;

    DemandeDTO updateDemande(Integer demandeId, @Valid DemandeInputDTO demande, Integer usagerId);

    DemandeComplementsDTO repondreDemandeComplements(Integer demandeId, Integer icId,
            @Valid DemandeComplementsReponseDTO reponse) throws IOException, TikaException, SAXException;

    DemandeDTO getDemande(Integer usagerId, Integer demandeId);

    byte[] getDemandeRecap(Integer usagerId, Integer demandeId, DonneesMConnectDTO donneesMConnectDTO);

    Page<DemandeDTO> getDemandesPageable(Integer usagerId, PageParamDTO pageParamDTO);

    List<DemandeComplementsDTO> getDemandeComplements(Integer demandeId);

    DemandeComplementsDTO getDemandeComplements(Integer demandeId, Integer icId);

    DemandeDTO associerDemandeCourrier(String identifiantDemande, String nomProprio, Integer usagerId);

    void desinscriptionUsager(Integer usagerId, String langue, boolean b);

    AccessDTO createOrUpdateAccess(Integer usagerId, @Valid AccessInputDTO dto);

    AccessDTO getAccess(Integer usagerId);

    UsagerCourrierDTO getUsagerCourrier(Integer usagerCourrierId);

    List<MotifDTO> getMotifs();

    List<PeriodeOuvertureDTO> getPeriodesOuverture();

    JsonNode getDonneesExternes(Integer usagerId, Map<String, String[]> parameterMap) throws Exception;

    List<PropertiesDTO> getFrontProperties();

    BrouillonDTO creerBrouillon(@Valid BrouillonDTO brouillon, Integer usagerId);

    BrouillonDTO updateBrouillon(@Valid BrouillonDTO brouillon, Integer usagerId);

    BrouillonDTO getBrouillon(Integer brouillonId, Integer usagerId);

    void deleteBrouillon(Integer brouillonId, Integer usagerId);

    Page<BrouillonDTO> getBrouillonsPageable(Integer usagerId, PageParamDTO pageParamDTO);

    JsonNode creerConfig(JsonNode config);

    List<PaysDTO> getPays();
    
    void deleteFile(String file);
    
    DemandeDTO lockDemande(Integer demandeId, Integer usagerId, Long timestamp) throws JsonProcessingException;

    DemandeDTO unlockDemande(Integer demandeId, Integer usagerId) throws JsonProcessingException;

    ResponseEntity getCustomRequest(HttpServletRequest request, Integer usagerId);

    ResponseEntity postCustomRequest(HttpServletRequest request, Integer usagerId);

    ResponseEntity putCustomRequest(HttpServletRequest request, Integer usagerId);

    ResponseEntity deleteCustomRequest(HttpServletRequest request, Integer usagerId);

}

package mc.gouv.xaf.back.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.tika.exception.TikaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeInputDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.dto.Page;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;

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
     * @param fromGU
     *            true si la désinscription du TS est demandée à cause d'une désinscription depuis le Guichet Unique
     */
    void desinscriptionUsager(Integer usagerId, String langue, boolean fromGU);

    AccessDTO createOrUpdateAccess(Integer usagerId, AccessInputDTO dto);

    AccessDTO getAccess(Integer usagerId);

    UsagerCourrierDTO getUsagerCourrier(Integer usagerCourrierId);

    List<MotifDTO> getMotifs();

    DemandeDTO creerDemande(DemandeInputDTO demande, Integer usagerId) throws JsonProcessingException;

    default DemandeDTO updateDemande(Integer demandeId, DemandeInputDTO demande, Integer usagerId)
            throws JsonProcessingException {
        return new DemandeDTO();
    }

    default DemandeDTO lockDemande(Integer demandeId, Integer usagerId, Long timestamp) throws JsonProcessingException {
        return new DemandeDTO();
    }

    default DemandeDTO unlockDemande(Integer demandeId, Integer usagerId) throws JsonProcessingException {
        return new DemandeDTO();
    }

    List<PeriodeOuvertureDTO> getPeriodesOuverture();

    @SuppressWarnings("rawtypes")
    default ResponseEntity getCustomRequest(HttpServletRequest request, Integer usagerId){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @SuppressWarnings("rawtypes")
    default ResponseEntity postCustomRequest(HttpServletRequest request, Integer usagerId){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @SuppressWarnings("rawtypes")
    default ResponseEntity putCustomRequest(HttpServletRequest request, Integer usagerId){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @SuppressWarnings("rawtypes")
    default ResponseEntity deleteCustomRequest(HttpServletRequest request, Integer usagerId){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    List<PropertiesDTO> getFrontProperties();

    BrouillonDTO creerBrouillon(BrouillonDTO brouillon, Integer usagerId);

    BrouillonDTO updateBrouillon(BrouillonDTO brouillon, Integer usagerId);

    List<BrouillonDTO> getBrouillons(Integer usagerId);

    Page<BrouillonDTO> getBrouillonsPageable(Integer usagerId, PageParamDTO paramDTO);

    BrouillonDTO getBrouillon(Integer pkBrouillons, Integer usagerId);

    void deleteBrouillon(Integer pkBrouillons, Integer usagerId) throws JsonProcessingException;

    default JsonNode getDonneesExternes(Integer usagerId, Map<String, String[]> params) throws JsonProcessingException {
        return null;
    }
}

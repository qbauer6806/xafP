package mc.gouv.xaf.back.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
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
import mc.gouv.xapi.error.dto.ErrorsDTO;
import mc.gouv.xapi.error.exception.WebException;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXException;

/**
 *
 * Interface reprenant les méthodes devant être implémentées dans les Web Services BACK, mais en y ajoutant les mappings
 * REST de Spring
 *
 * @author qdeme
 * @author fgaujous
 *
 */
public abstract class AbstractAfApiController implements AfApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAfApiController.class);

    @PutMapping(value = "/demandes/{demandeId}/annuler")
    public void annulerDemandeRequest(@PathVariable(value = "demandeId") Integer demandeId,
            @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.annulerDemande({}, {})", demandeId, usagerId);
        annulerDemande(demandeId, usagerId);
    }

    @PostMapping(value = "/demandes")
    public DemandeDTO creerDemandeRequest(@Valid @RequestBody DemandeInputDTO demande,
            @RequestParam(value = "usagerId") Integer usagerId, HttpServletRequest request)
            throws JsonProcessingException {
        LOGGER.info("AbstractAfApiController.creerDemande({}, {})", demande, usagerId);
        return creerDemande(demande, usagerId);
    }

    @PutMapping(value = "/demandes/{demandeId}")
    public DemandeDTO updateDemandeRequest(@PathVariable(value = "demandeId") Integer demandeId,
            @Valid @RequestBody DemandeInputDTO demande, @RequestParam(value = "usagerId") Integer usagerId,
            HttpServletRequest request) throws JsonProcessingException {
        LOGGER.info("AbstractAfApiController.updateDemande({}, {}, {})", demandeId, demande, usagerId);

        return updateDemande(demandeId, demande, usagerId);
    }

    @PutMapping(value = "/demandes/{demandeId}/lock")
    public DemandeDTO updateDemandeLockRequest(@PathVariable(value = "demandeId") Integer demandeId,
            @RequestParam(value = "usagerId") Integer usagerId, @RequestParam(value = "timestamp") Long timestamp,
            HttpServletRequest request) throws JsonProcessingException {
        LOGGER.info("AbstractAfApiController.updateDemandeLockRequest({}, {})", demandeId, usagerId);

        return lockDemande(demandeId, usagerId, timestamp);
    }

    @PutMapping(value = "/demandes/{demandeId}/unlock")
    public DemandeDTO updateDemandeUnlockRequest(@PathVariable(value = "demandeId") Integer demandeId,
            @RequestParam(value = "usagerId") Integer usagerId, HttpServletRequest request)
            throws JsonProcessingException {
        LOGGER.info("AbstractAfApiController.updateDemandeLockRequest({}, {})", demandeId, usagerId);

        return unlockDemande(demandeId, usagerId);
    }

    @PutMapping(value = "/demandes/{demandeId}/complements/{icId}")
    public DemandeComplementsDTO repondreDemandeComplementsRequest(@PathVariable(value = "demandeId") Integer demandeId,
            @PathVariable(value = "icId") Integer icId, @Valid @RequestBody DemandeComplementsReponseDTO reponse)
            throws IOException, TikaException, SAXException {
        LOGGER.info("AbstractAfApiController.repondreDemandeComplements({}, {}, {})", demandeId, icId, reponse);
        return repondreDemandeComplements(demandeId, icId, reponse);
    }

    @GetMapping(value = "/usagers/{usagerId}/demandes/{demandeId}")
    public @ResponseBody DemandeDTO getDemandeRequest(@PathVariable(value = "usagerId") Integer usagerId,
            @PathVariable(value = "demandeId") Integer demandeId) {
        LOGGER.info("AbstractAfApiController.getDemande({}, {})", usagerId, demandeId);
        return getDemande(usagerId, demandeId);
    }

    @GetMapping(value = "/demandes")
    public @ResponseBody List<DemandeDTO> getDemandesRequest(@RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.getDemandes({})", usagerId);
        return getDemandes(usagerId);
    }

    @GetMapping(value = "/demandespage")
    public @ResponseBody Page<DemandeDTO> getDemandesPageableRequest(@RequestParam(value = "usagerId") Integer usagerId,
            @RequestParam int page, @RequestParam int size, @RequestParam String sort, @RequestParam String direction,
            @RequestParam String status, @RequestParam String lang) {
        LOGGER.info("AbstractAfApiController.getDemandesPageable({})", usagerId);
        return getDemandesPageable(usagerId, new PageParamDTO(page, size, sort, direction, status, lang));
    }

    @GetMapping(value = "/demandes/{demandeId}/complements")
    public @ResponseBody List<DemandeComplementsDTO> getDemandeComplementsRequest(
            @PathVariable(value = "demandeId") Integer demandeId) {
        LOGGER.info("AbstractAfApiController.getDemandeComplements({})", demandeId);
        return getDemandeComplements(demandeId);
    }

    @GetMapping(value = "/demandes/{demandeId}/complements/{icId}")
    public @ResponseBody DemandeComplementsDTO getDemandeComplementsRequest(
            @PathVariable(value = "demandeId") Integer demandeId, @PathVariable(value = "icId") Integer icId) {
        LOGGER.info("AbstractAfApiController.getDemandeComplements({}, {})", demandeId, icId);
        return getDemandeComplements(demandeId, icId);
    }

    @PostMapping(value = "/demandes/associerDemandeCourrier")
    public DemandeDTO associerDemandeCourrierRequest(
            @RequestParam(value = "identifiantDemande") String identifiantDemande,
            @RequestParam(value = "nomProprio") String nomProprio, @RequestParam(value = "usagerId") Integer usagerId) {
        String safeIndentifiant = AfBackUtils.logSafe(identifiantDemande);
        String safeNom = AfBackUtils.logSafe(nomProprio);
        LOGGER.info("AbstractAfApiController.associerDemandeCourrierRequest({}, {}, {})", safeIndentifiant, safeNom,
                usagerId);
        return associerDemandeCourrier(identifiantDemande, nomProprio, usagerId);
    }

    @DeleteMapping(value = "/accesses/{usagerId}")
    public void desinscriptionUsagerRequest(@PathVariable(value = "usagerId") Integer usagerId,
            @RequestParam(value = "langue") String langue) {
        String safeLangue = AfBackUtils.logSafe(langue);
        LOGGER.info("AbstractAfApiController.desinscriptionUsagerRequest({}, {})", usagerId, safeLangue);
        desinscriptionUsager(usagerId, langue, false);
    }

    @PostMapping(value = "/accesses/{usagerId}")
    public AccessDTO createOrUpdateAccessRequest(@PathVariable(value = "usagerId") Integer usagerId,
            @Valid @RequestBody AccessInputDTO dto) {
        LOGGER.info("AbstractAfApiController.createOrUpdateAccessRequest({}, +dto)", usagerId);
        return createOrUpdateAccess(usagerId, dto);
    }

    @GetMapping(value = "/accesses/{usagerId}")
    public AccessDTO getAccessRequest(@PathVariable(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.getAccessRequest({})", usagerId);
        return getAccess(usagerId);
    }

    @GetMapping(value = "/usagerscourrier/{usagerCourrierId}")
    public UsagerCourrierDTO getUsagerCourrierRequest(
            @PathVariable(value = "usagerCourrierId") Integer usagerCourrierId) {
        LOGGER.info("AbstractAfApiController.getUsagerCourrierRequest({})", usagerCourrierId);
        return getUsagerCourrier(usagerCourrierId);
    }

    @GetMapping(value = "/motifs")
    public List<MotifDTO> getMotifsRequest() {
        LOGGER.info("AbstractAfApiController.getMotifsRequest()");
        return getMotifs();
    }

    @GetMapping(value = "/periodesouverture")
    public List<PeriodeOuvertureDTO> getPeriodesOuvertureRequest() {
        LOGGER.info("AbstractAfApiController.getPeriodesOuverture()");
        return getPeriodesOuverture();
    }

    @GetMapping(value = "/donneesexternes")
    public JsonNode getDonneesExternesRequest(HttpServletRequest request,
            @RequestParam(value = "usagerId") Integer usagerId) throws IOException {
        LOGGER.info("AbstractAfApiController.getDonneesExternesRequest()");
        return getDonneesExternes(usagerId, request.getParameterMap());
    }

    @GetMapping(value = "/properties")
    public List<PropertiesDTO> getFrontPropertiesRequest() {
        LOGGER.info("AbstractAfApiController.getFrontPropertiesRequest()");
        return getFrontProperties();
    }

    @SuppressWarnings("rawtypes")
	@GetMapping(value = "/customRequest/**")
    public ResponseEntity getCustomRequestRequest(HttpServletRequest request,
                                                  @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.getCustomRequest()");
        return getCustomRequest(request, usagerId);
    }

    @SuppressWarnings("rawtypes")
	@PostMapping(value = "/customRequest/**")
    public ResponseEntity postCustomRequestRequest(HttpServletRequest request,
                                                   @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.postCustomRequest()");
        return postCustomRequest(request, usagerId);
    }

    @SuppressWarnings("rawtypes")
	@PutMapping(value = "/customRequest/**")
    public ResponseEntity putCustomRequestRequest(HttpServletRequest request,
                                                  @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.putCustomRequest()");
        return putCustomRequest(request, usagerId);
    }

    @SuppressWarnings("rawtypes")
	@DeleteMapping(value = "/customRequest/**")
    public ResponseEntity deleteCustomRequestRequest(HttpServletRequest request,
                                                     @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.deleteCustomRequest()");
        return deleteCustomRequest(request, usagerId);
    }

    @PostMapping(value = "/brouillons")
    public BrouillonDTO creerBrouillonRequest(@Valid @RequestBody BrouillonDTO brouillon,
            @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.creerBrouillonRequest({}, {})", brouillon, usagerId);
        return creerBrouillon(brouillon, usagerId);
    }

    @PutMapping(value = "/brouillons/{brouillonId}")
    public BrouillonDTO updateBrouillonRequest(@Valid @RequestBody BrouillonDTO brouillon,
            @PathVariable(value = "brouillonId") Integer brouillonId,
            @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.updateBrouillonRequest({}, {}, {})", brouillon, brouillonId, usagerId);
        brouillon.setPkBrouillons(brouillonId);
        return updateBrouillon(brouillon, usagerId);
    }

    @GetMapping(value = "/brouillons")
    public @ResponseBody List<BrouillonDTO> getBrouillonsRequest(@RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.getBrouillonsRequest({})", usagerId);
        return getBrouillons(usagerId);
    }

    @GetMapping(value = "/brouillons/{brouillonId}")
    public @ResponseBody BrouillonDTO getBrouillonRequest(@PathVariable(value = "brouillonId") Integer brouillonId,
            @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.getBrouillonRequest({}, {})", brouillonId, usagerId);
        return getBrouillon(brouillonId, usagerId);
    }

    @DeleteMapping(value = "/brouillons/{brouillonId}")
    public void deleteBrouillonRequest(@PathVariable(value = "brouillonId") Integer brouillonId,
            @RequestParam(value = "usagerId") Integer usagerId) throws JsonProcessingException {
        LOGGER.info("AbstractAfApiController.deleteBrouillonRequest({}, {})", brouillonId, usagerId);
        deleteBrouillon(brouillonId, usagerId);
    }

    @GetMapping(value = "/brouillonspage")
    public @ResponseBody Page<BrouillonDTO> getBrouillonsPageableRequest(
            @RequestParam(value = "usagerId") Integer usagerId, @RequestParam int page, @RequestParam int size,
            @RequestParam String sort, @RequestParam String direction) {
        LOGGER.info("AbstractAfApiController.getBrouillonsPageable({})", usagerId);
        return getBrouillonsPageable(usagerId, new PageParamDTO(page, size, sort, direction, null, null));
    }

    @ExceptionHandler(WebException.class)
    public @ResponseBody ErrorsDTO handleMetierWebException(HttpServletResponse res, WebException ex) {
        LOGGER.error("handleMetierWebException : " + ex.getMessage(), ex);
        ErrorsDTO errorsDTO = new ErrorsDTO();
        errorsDTO.setHttpStatus(ex.getHttpStatus());
        errorsDTO.setMessage(ex.getMessage());
        errorsDTO.setErrors(ex.getErrors());
        res.setStatus(ex.getHttpStatus());
        return errorsDTO;
    }

    @PostMapping(value = "/configs")
    public JsonNode creerConfigRequest(@RequestBody JsonNode config) {
        LOGGER.info("AbstractAfApiController.creerConfigRequest");
        return creerConfig(config);
    }

}

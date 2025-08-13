package mc.gouv.xaf.api.controller;

import java.io.IOException;
import java.util.List;

import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.AfApi;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
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
import mc.gouv.xapi.error.dto.ErrorsDTO;
import mc.gouv.xapi.error.exception.WebException;

/**
 * Controller exposant l'API REST destinée à être appelée par le FO.
 *
 * @author qdeme
 */
@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class AfApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfApiController.class);

    @Autowired
    private AfApi afApiService;

    @Autowired
    private DemandesTransformer demandesTransformer;

    @Autowired
    private DemandesComplementsTransformer demandesComplementsTransformer;

    @PutMapping(value = "/demandes/{demandeId}/annuler")
    public void annulerDemandeRequest(@PathVariable(value = "demandeId") Integer demandeId,
            @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.annulerDemande({}, {})", demandeId, usagerId);
        afApiService.annulerDemande(demandeId, usagerId);
    }

    @PostMapping(value = "/demandes")
    public DemandeDTO creerDemandeRequest(@Valid @RequestBody DemandeInputDTO demande,
            @RequestParam(value = "usagerId") Integer usagerId) throws JsonProcessingException {
        LOGGER.debug("AbstractAfApiController.creerDemande({}, {})", demande, usagerId);
        return afApiService.creerDemande(demande, usagerId);
    }

    @PutMapping(value = "/demandes/{demandeId}")
    public DemandeDTO updateDemandeRequest(@PathVariable(value = "demandeId") Integer demandeId,
            @Valid @RequestBody DemandeInputDTO demande, @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.updateDemande({}, {}, {})", demandeId, demande, usagerId);
        DemandeDTO demandeDTO = afApiService.updateDemande(demandeId, demande, usagerId);
        demandesTransformer.hideInfos(demandeDTO);
        return demandeDTO;
    }

    @PutMapping(value = "/demandes/{demandeId}/lock")
    public DemandeDTO updateDemandeLockRequest(@PathVariable(value = "demandeId") Integer demandeId,
            @RequestParam(value = "usagerId") Integer usagerId, @RequestParam(value = "timestamp") Long timestamp) throws JsonProcessingException {
        LOGGER.info("AbstractAfApiController.updateDemandeLockRequest({}, {})", demandeId, usagerId);
        return afApiService.lockDemande(demandeId, usagerId, timestamp);
    }

    @PutMapping(value = "/demandes/{demandeId}/unlock")
    public DemandeDTO updateDemandeUnlockRequest(@PathVariable(value = "demandeId") Integer demandeId,
            @RequestParam(value = "usagerId") Integer usagerId, HttpServletRequest request) throws JsonProcessingException {
        LOGGER.info("AbstractAfApiController.updateDemandeLockRequest({}, {})", demandeId, usagerId);
        return afApiService.unlockDemande(demandeId, usagerId);
    }

    @PutMapping(value = "/demandes/{demandeId}/complements/{icId}")
    public DemandeComplementsDTO repondreDemandeComplementsRequest(@PathVariable(value = "demandeId") Integer demandeId,
            @PathVariable(value = "icId") Integer icId, @Valid @RequestBody DemandeComplementsReponseDTO reponse)
            throws IOException, TikaException, SAXException {
        LOGGER.info("AbstractAfApiController.repondreDemandeComplements({}, {}, {})", demandeId, icId, reponse);
        return afApiService.repondreDemandeComplements(demandeId, icId, reponse);
    }

    @GetMapping(value = "/usagers/{usagerId}/demandes/{demandeId}")
    public @ResponseBody DemandeDTO getDemandeRequest(@PathVariable(value = "usagerId") Integer usagerId,
            @PathVariable(value = "demandeId") Integer demandeId) {
        LOGGER.info("AbstractAfApiController.getDemande({}, {})", usagerId, demandeId);
        DemandeDTO demandeDTO = afApiService.getDemande(usagerId, demandeId);
        demandesTransformer.hideInfos(demandeDTO);
        return demandeDTO;
    }

    @PostMapping(value = "/usagers/{usagerId}/demandes/recap/{demandeId}")
    public @ResponseBody byte[] getDemandeRecapRequest(@PathVariable(value = "usagerId") Integer usagerId,
            @PathVariable(value = "demandeId") Integer demandeId,
            @RequestBody(required = false) DonneesMConnectDTO donneesMConnectDTO) {
        LOGGER.info("AbstractAfApiController.getDemandeRecapRequest({}, {}, {})", usagerId, demandeId,
                donneesMConnectDTO);
        return afApiService.getDemandeRecap(usagerId, demandeId, donneesMConnectDTO);
    }

    @GetMapping(value = "/demandes")
    public @ResponseBody List<DemandeDTO> getDemandesRequest(@RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.getDemandes({})", usagerId);
        List<DemandeDTO> demandeDTOS = afApiService.getDemandes(usagerId);
        demandesTransformer.hideInfos(demandeDTOS);
        return demandeDTOS;
    }

    @GetMapping(value = "/demandespage")
    public @ResponseBody Page<DemandeDTO> getDemandesPageableRequest(@RequestParam(value = "usagerId") Integer usagerId,
            @RequestParam int page, @RequestParam int size, @RequestParam String sort, @RequestParam String direction,
            @RequestParam String status, @RequestParam String lang) {
        LOGGER.info("AbstractAfApiController.getDemandesPageable({})", usagerId);
        Page<DemandeDTO> demandeDTOS = afApiService.getDemandesPageable(usagerId,
                new PageParamDTO(page, size, sort, direction, status, lang));
        demandesTransformer.hideInfosPageable(demandeDTOS.getContent());
        return demandeDTOS;
    }

    @GetMapping(value = "/demandes/{demandeId}/complements")
    public @ResponseBody List<DemandeComplementsDTO> getDemandeComplementsRequest(
            @PathVariable(value = "demandeId") Integer demandeId) {
        LOGGER.info("AbstractAfApiController.getDemandeComplements({})", demandeId);
        List<DemandeComplementsDTO> demandeComplementsDTOS = afApiService.getDemandeComplements(demandeId);
        demandesComplementsTransformer.hideInfos(demandeComplementsDTOS);
        return demandeComplementsDTOS;
    }

    @GetMapping(value = "/demandes/{demandeId}/complements/{icId}")
    public @ResponseBody DemandeComplementsDTO getDemandeComplementsRequest(
            @PathVariable(value = "demandeId") Integer demandeId, @PathVariable(value = "icId") Integer icId) {
        LOGGER.info("AbstractAfApiController.getDemandeComplements({}, {})", demandeId, icId);
        DemandeComplementsDTO demandeComplementsDTO = afApiService.getDemandeComplements(demandeId, icId);
        demandesComplementsTransformer.hideInfos(demandeComplementsDTO);
        return demandeComplementsDTO;
    }

    @PostMapping(value = "/demandes/associerDemandeCourrier")
    public DemandeDTO associerDemandeCourrierRequest(
            @RequestParam(value = "identifiantDemande") String identifiantDemande,
            @RequestParam(value = "nomProprio") String nomProprio, @RequestParam(value = "usagerId") Integer usagerId) {
        String safeIndentifiant = AfBackUtils.logSafe(identifiantDemande);
        String safeNom = AfBackUtils.logSafe(nomProprio);
        LOGGER.info("AbstractAfApiController.associerDemandeCourrierRequest({}, {}, {})", safeIndentifiant, safeNom,
                usagerId);
        DemandeDTO demandeDTO = afApiService.associerDemandeCourrier(identifiantDemande, nomProprio, usagerId);
        demandesTransformer.hideInfos(demandeDTO);
        return demandeDTO;
    }

    @DeleteMapping(value = "/accesses/{usagerId}")
    public void desinscriptionUsagerRequest(@PathVariable(value = "usagerId") Integer usagerId,
            @RequestParam(value = "langue") String langue) {
        String safeLangue = AfBackUtils.logSafe(langue);
        LOGGER.info("AbstractAfApiController.desinscriptionUsagerRequest({}, {})", usagerId, safeLangue);
        afApiService.desinscriptionUsager(usagerId, langue, false);
    }

    @PostMapping(value = "/accesses/{usagerId}")
    public AccessDTO createOrUpdateAccessRequest(@PathVariable(value = "usagerId") Integer usagerId,
            @Valid @RequestBody AccessInputDTO dto) {
        LOGGER.info("AbstractAfApiController.createOrUpdateAccessRequest({}, +dto)", usagerId);
        return afApiService.createOrUpdateAccess(usagerId, dto);
    }

    @GetMapping(value = "/accesses/{usagerId}")
    public AccessDTO getAccessRequest(@PathVariable(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.getAccessRequest({})", usagerId);
        return afApiService.getAccess(usagerId);
    }

    @GetMapping(value = "/usagerscourrier/{usagerCourrierId}")
    public UsagerCourrierDTO getUsagerCourrierRequest(
            @PathVariable(value = "usagerCourrierId") Integer usagerCourrierId) {
        LOGGER.info("AbstractAfApiController.getUsagerCourrierRequest({})", usagerCourrierId);
        return afApiService.getUsagerCourrier(usagerCourrierId);
    }

    @GetMapping(value = "/motifs")
    public List<MotifDTO> getMotifsRequest() {
        LOGGER.info("AbstractAfApiController.getMotifsRequest()");
        return afApiService.getMotifs();
    }

    @GetMapping(value = "/periodesouverture")
    public List<PeriodeOuvertureDTO> getPeriodesOuvertureRequest() {
        LOGGER.info("AbstractAfApiController.getPeriodesOuverture()");
        return afApiService.getPeriodesOuverture();
    }

    @GetMapping(value = "/donneesexternes")
    public JsonNode getDonneesExternesRequest(HttpServletRequest request,
            @RequestParam(value = "usagerId") Integer usagerId) throws Exception {
        LOGGER.info("AbstractAfApiController.getDonneesExternesRequest()");
        return afApiService.getDonneesExternes(usagerId, request.getParameterMap());
    }

    @GetMapping(value = "/properties")
    public List<PropertiesDTO> getFrontPropertiesRequest() {
        LOGGER.info("AbstractAfApiController.getFrontPropertiesRequest()");
        return afApiService.getFrontProperties();
    }

    @SuppressWarnings("rawtypes")
    @GetMapping(value = "/customRequest/**")
    public ResponseEntity getCustomRequestRequest(HttpServletRequest request,
            @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.getCustomRequest()");
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @SuppressWarnings("rawtypes")
    @PostMapping(value = "/customRequest/**")
    public ResponseEntity postCustomRequestRequest(HttpServletRequest request,
            @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.postCustomRequest()");
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @SuppressWarnings("rawtypes")
    @PutMapping(value = "/customRequest/**")
    public ResponseEntity putCustomRequestRequest(HttpServletRequest request,
            @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.putCustomRequest()");
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @SuppressWarnings("rawtypes")
    @DeleteMapping(value = "/customRequest/**")
    public ResponseEntity deleteCustomRequestRequest(HttpServletRequest request,
            @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.deleteCustomRequest()");
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping(value = "/brouillons")
    public BrouillonDTO creerBrouillonRequest(@Valid @RequestBody BrouillonDTO brouillon,
            @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.creerBrouillonRequest({}, {})", brouillon, usagerId);
        return afApiService.creerBrouillon(brouillon, usagerId);
    }

    @PutMapping(value = "/brouillons/{brouillonId}")
    public BrouillonDTO updateBrouillonRequest(@Valid @RequestBody BrouillonDTO brouillon,
            @PathVariable(value = "brouillonId") Integer brouillonId,
            @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.updateBrouillonRequest({}, {}, {})", brouillon, brouillonId, usagerId);
        brouillon.setPkBrouillons(brouillonId);
        return afApiService.updateBrouillon(brouillon, usagerId);
    }

    @GetMapping(value = "/brouillons/{brouillonId}")
    public @ResponseBody BrouillonDTO getBrouillonRequest(@PathVariable(value = "brouillonId") Integer brouillonId,
            @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.getBrouillonRequest({}, {})", brouillonId, usagerId);
        return afApiService.getBrouillon(brouillonId, usagerId);
    }

    @DeleteMapping(value = "/brouillons/{brouillonId}")
    public void deleteBrouillonRequest(@PathVariable(value = "brouillonId") Integer brouillonId,
            @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.deleteBrouillonRequest({}, {})", brouillonId, usagerId);
        afApiService.deleteBrouillon(brouillonId, usagerId);
    }

    @DeleteMapping(value = "/file/{accessId}/{uuid}/{filename}")
    public void deleteFileRequest(@PathVariable(required = false) String accessId,
            @PathVariable(required = false) String uuid, @PathVariable(required = false) String filename) {
        LOGGER.info("AbstractAfApiController.deleteFileRequest({},{},{})", accessId, uuid, filename);
        afApiService.deleteFile("/" + accessId + "/" + uuid + "/" + filename);
    }

    @GetMapping(value = "/brouillonspage")
    public @ResponseBody Page<BrouillonDTO> getBrouillonsPageableRequest(
            @RequestParam(value = "usagerId") Integer usagerId, @RequestParam int page, @RequestParam int size,
            @RequestParam String sort, @RequestParam String direction) {
        LOGGER.info("AbstractAfApiController.getBrouillonsPageable({})", usagerId);
        return afApiService.getBrouillonsPageable(usagerId, new PageParamDTO(page, size, sort, direction, null, null));
    }

    @ExceptionHandler({ WebException.class })
    public @ResponseBody ErrorsDTO handleException(HttpServletResponse res, WebException ex) {
        LOGGER.error("Exception : {}", ex.getMessage(), ex);
        ErrorsDTO errorsDTO = new ErrorsDTO();
        errorsDTO.setHttpStatus(ex.getHttpStatus());
        errorsDTO.setMessage(ex.getMessage());
        errorsDTO.setErrors(ex.getErrors());
        res.setStatus(ex.getHttpStatus());
        return errorsDTO;
    }

    @ExceptionHandler(DemarchesServiceException.class)
    public @ResponseBody ErrorsDTO handleDemarchesException(DemarchesServiceException dse, HttpServletResponse resp) {
        LOGGER.info("Exception :", dse);
        ErrorsDTO errorsDTO = new ErrorsDTO();
        errorsDTO.setHttpStatus(dse.getHttpStatus().value());
        errorsDTO.setMessage(dse.getMessage());
        resp.setStatus(dse.getHttpStatus().value());
        return errorsDTO;
    }

    @PostMapping(value = "/configs")
    public JsonNode creerConfigRequest(@RequestBody JsonNode config) {
        LOGGER.info("AbstractAfApiController.creerConfigRequest");
        return afApiService.creerConfig(config);
    }
    
    @GetMapping(value = "/pays")
    public List<PaysDTO> getPays() {
        LOGGER.info("AbstractAfApiController.getPays()");
        return afApiService.getPays();
    }

}

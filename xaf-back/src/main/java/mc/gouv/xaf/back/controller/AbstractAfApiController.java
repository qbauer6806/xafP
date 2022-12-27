package mc.gouv.xaf.back.controller;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.tika.exception.TikaException;
import org.hibernate.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;

import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.es.impl.IndexedEsDemandeServiceImpl;
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
    private static final String REINDEX_MESSAGE = "Le nombre de demandes réindexées est {0}";

    @Autowired
    private DemandesService demandesService;

    @RequestMapping(value = "/demandes/{demandeId}/annuler", method = RequestMethod.PUT)
    public void annulerDemandeRequest(@PathVariable(value = "demandeId") Integer demandeId,
            @RequestParam(value = "usagerId", required = true) Integer usagerId) {
        LOGGER.info("AbstractAfApiController.annulerDemande(" + demandeId + "," + usagerId + ")");
        annulerDemande(demandeId, usagerId);
    }

    @RequestMapping(value = "/demandes", method = RequestMethod.POST)
    public DemandeDTO creerDemandeRequest(@Valid @RequestBody DemandeInputDTO demande,
            @RequestParam(value = "usagerId", required = true) Integer usagerId, HttpServletRequest request)
            throws JsonProcessingException {
        LOGGER.info("AbstractAfApiController.creerDemande(" + demande + "," + usagerId + ")");

        return creerDemande(demande, usagerId);
    }

    @RequestMapping(value = "/demandes/{demandeId}/complements/{icId}", method = RequestMethod.PUT)
    public DemandeComplementsDTO repondreDemandeComplementsRequest(@PathVariable(value = "demandeId") Integer demandeId,
            @PathVariable(value = "icId") Integer icId, @Valid @RequestBody DemandeComplementsReponseDTO reponse)
            throws Exception {
        LOGGER.info(
                "AbstractAfApiController.repondreDemandeComplements(" + demandeId + "," + icId + "," + reponse + ")");
        return repondreDemandeComplements(demandeId, icId, reponse);
    }

    @RequestMapping(value = "/usagers/{usagerId}/demandes/{demandeId}", method = RequestMethod.GET)
    public @ResponseBody DemandeDTO getDemandeRequest(@PathVariable(value = "usagerId") Integer usagerId,
            @PathVariable(value = "demandeId") Integer demandeId) {
        LOGGER.info("AbstractAfApiController.getDemande(" + usagerId + "," + demandeId + ")");
        return getDemande(usagerId, demandeId);
    }

    @RequestMapping(value = "/demandes", method = RequestMethod.GET)
    public @ResponseBody List<DemandeDTO> getDemandesRequest(
            @RequestParam(value = "usagerId", required = true) Integer usagerId) {
        LOGGER.info("AbstractAfApiController.getDemandes(" + usagerId + ")");
        return getDemandes(usagerId);
    }

    @GetMapping(value = "/demandespage")
    public @ResponseBody
    Page<DemandeDTO> getDemandesPageableRequest(@RequestParam(value = "usagerId") Integer usagerId, @RequestParam int page,
                                                @RequestParam int size, @RequestParam String sort, @RequestParam String direction,
                                                @RequestParam String status, @RequestParam String lang) {
        LOGGER.info("AbstractAfApiController.getDemandesPageable({})", usagerId);
        return getDemandesPageable(usagerId, new PageParamDTO(page, size, sort, direction, status, lang));
    }

    @RequestMapping(value = "/demandes/{demandeId}/complements", method = RequestMethod.GET)
    public @ResponseBody List<DemandeComplementsDTO> getDemandeComplementsRequest(
            @PathVariable(value = "demandeId") Integer demandeId) {
        LOGGER.info("AbstractAfApiController.getDemandeComplements(" + demandeId + ")");
        return getDemandeComplements(demandeId);
    }

    @RequestMapping(value = "/demandes/{demandeId}/complements/{icId}", method = RequestMethod.GET)
    public @ResponseBody DemandeComplementsDTO getDemandeComplementsRequest(
            @PathVariable(value = "demandeId") Integer demandeId, @PathVariable(value = "icId") Integer icId) {
        LOGGER.info("AbstractAfApiController.getDemandeComplements(" + demandeId + "," + icId + ")");
        return getDemandeComplements(demandeId, icId);
    }

    @RequestMapping(value = "/demandes/associerDemandeCourrier", method = RequestMethod.POST)
    public DemandeDTO associerDemandeCourrierRequest(
            @RequestParam(value = "identifiantDemande", required = true) String identifiantDemande,
            @RequestParam(value = "nomProprio", required = true) String nomProprio,
            @RequestParam(value = "usagerId", required = true) Integer usagerId) {
        LOGGER.info("AbstractAfApiController.associerDemandeCourrierRequest(" + identifiantDemande + "," + nomProprio
                + "," + usagerId + ")");
        return associerDemandeCourrier(identifiantDemande, nomProprio, usagerId);
    }

    @RequestMapping(value = "/accesses/{usagerId}", method = RequestMethod.DELETE)
	public void desinscriptionUsagerRequest(@PathVariable(value = "usagerId") Integer usagerId,
                                            @RequestParam(value = "langue", required = true) String langue) {
        LOGGER.info("AbstractAfApiController.desinscriptionUsagerRequest({}, {})", usagerId, langue);
		desinscriptionUsager(usagerId, langue, false);
	}

    @RequestMapping(value = "/accesses/{usagerId}", method = RequestMethod.POST)
    public AccessDTO createOrUpdateAccessRequest(@PathVariable(value = "usagerId") Integer usagerId,
            @Valid @RequestBody AccessInputDTO dto) {
        LOGGER.info("AbstractAfApiController.createOrUpdateAccessRequest(" + usagerId + " (+dto))");
        return createOrUpdateAccess(usagerId, dto);
    }

    @RequestMapping(value = "/accesses/{usagerId}", method = RequestMethod.GET)
    public AccessDTO getAccessRequest(@PathVariable(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.getAccessRequest(" + usagerId + ")");
        return getAccess(usagerId);
    }

    @RequestMapping(value = "/usagerscourrier/{usagerCourrierId}", method = RequestMethod.GET)
    public UsagerCourrierDTO getUsagerCourrierRequest(
            @PathVariable(value = "usagerCourrierId") Integer usagerCourrierId) {
        LOGGER.info("AbstractAfApiController.getUsagerCourrierRequest(" + usagerCourrierId + ")");
        return getUsagerCourrier(usagerCourrierId);
    }

    @RequestMapping(value = "/motifs", method = RequestMethod.GET)
    public List<MotifDTO> getMotifsRequest() {
        LOGGER.info("AbstractAfApiController.getMotifsRequest()");
        return getMotifs();
    }

    @RequestMapping(value = "/reindex", method = RequestMethod.POST)
    public String reindex() throws IOException, SAXException, TikaException {

        LOGGER.info("======================= Appel de /ws/demandes/reindex");

        if (demandesService instanceof IndexedEsDemandeServiceImpl) {
            try {
                Long demandesCount = ((IndexedEsDemandeServiceImpl) demandesService).reindex();
                return MessageFormat.format(REINDEX_MESSAGE, demandesCount);
            } catch (TransactionException e) {
                if (e.getCause() != null) {
                    return e.getCause().getMessage();
                }
                return e.getMessage();
            } finally {
                LOGGER.info("======================= Fin appel de /ws/demandes/reindex");
            }
        } else {
            LOGGER.info("======================= Fin appel de /ws/demandes/reindex");
            return "Indexing is disabled, please enable it";
        }

    }

    @RequestMapping(value = "/periodesouverture", method = RequestMethod.GET)
    public List<PeriodeOuvertureDTO> getPeriodesOuvertureRequest() {
        LOGGER.info("AbstractAfApiController.getPeriodesOuverture()");
        return getPeriodesOuverture();
    }

    @RequestMapping(value = "/properties", method = RequestMethod.GET)
    public List<PropertiesDTO> getFrontPropertiesRequest() {
        LOGGER.info("AbstractAfApiController.getFrontPropertiesRequest()");
        return getFrontProperties();
    }

    @SuppressWarnings("rawtypes")
	@RequestMapping(value = "/customRequest/**", method = RequestMethod.GET)
    public ResponseEntity getCustomRequestRequest(HttpServletRequest request) {
        LOGGER.info("AbstractAfApiController.getCustomRequest()");
        return getCustomRequest(request);
    }

    @SuppressWarnings("rawtypes")
	@RequestMapping(value = "/customRequest/**", method = RequestMethod.POST)
    public ResponseEntity postCustomRequestRequest(HttpServletRequest request) {
        LOGGER.info("AbstractAfApiController.postCustomRequest()");
        return postCustomRequest(request);
    }

    @SuppressWarnings("rawtypes")
	@RequestMapping(value = "/customRequest/**", method = RequestMethod.PUT)
    public ResponseEntity putCustomRequestRequest(HttpServletRequest request) {
        LOGGER.info("AbstractAfApiController.putCustomRequest()");
        return putCustomRequest(request);
    }

    @SuppressWarnings("rawtypes")
	@RequestMapping(value = "/customRequest/**", method = RequestMethod.DELETE)
    public ResponseEntity deleteCustomRequestRequest(HttpServletRequest request) {
        LOGGER.info("AbstractAfApiController.deleteCustomRequest()");
        return deleteCustomRequest(request);
    }
    
    @RequestMapping(value = "/brouillons", method = RequestMethod.POST)
    public BrouillonDTO creerBrouillonRequest(@Valid @RequestBody BrouillonDTO brouillon,
    		@RequestParam(value = "usagerId", required = true) Integer usagerId) {
        LOGGER.info("AbstractAfApiController.creerBrouillonRequest(" + brouillon + "," + usagerId + ")");

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
    
    @RequestMapping(value = "/brouillons", method = RequestMethod.GET)
    public @ResponseBody List<BrouillonDTO> getBrouillonsRequest(@RequestParam(value = "usagerId", required = true) Integer usagerId) {
        LOGGER.info("AbstractAfApiController.getBrouillonsRequest(" + usagerId + ")");

        return getBrouillons(usagerId);
    }
    
    @GetMapping(value = "/brouillons/{brouillonId}")
    public @ResponseBody BrouillonDTO getBrouillonRequest(@PathVariable(value = "brouillonId") Integer brouillonId,
                                                          @RequestParam(value = "usagerId") Integer usagerId) {
        LOGGER.info("AbstractAfApiController.getBrouillonRequest({}, {})", brouillonId, usagerId);
        return getBrouillon(brouillonId, usagerId);
    }
    
    @RequestMapping(value = "/brouillons/{brouillonId}", method = RequestMethod.DELETE)
    public void deleteBrouillonRequest(@PathVariable(value = "brouillonId") Integer brouillonId,
                                       @RequestParam(value = "usagerId") Integer usagerId) throws JsonProcessingException {
        LOGGER.info("AbstractAfApiController.deleteBrouillonRequest({}, {})", brouillonId, usagerId);
        deleteBrouillon(brouillonId, usagerId);
    }
    
    @GetMapping(value = "/brouillonspage")
    public @ResponseBody
    Page<BrouillonDTO> getBrouillonsPageableRequest(@RequestParam(value = "usagerId") Integer usagerId, @RequestParam int page,
                                                @RequestParam int size, @RequestParam String sort, @RequestParam String direction) {
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

}

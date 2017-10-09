package mc.gouv.af.apiserver;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import mc.gouv.dem.shared.model.AccessDTO;
import mc.gouv.dem.shared.model.AccessInputDTO;
import mc.gouv.dem.shared.model.DemandeComplementsDTO;
import mc.gouv.dem.shared.model.DemandeComplementsReponseDTO;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.DemandeInputDTO;
import mc.gouv.dem.shared.model.MotifDTO;
import mc.gouv.dem.shared.model.UsagerCourrierDTO;
import mc.gouv.xapi.error.dto.ErrorsDTO;
import mc.gouv.xapi.error.exception.WebException;

/**
 * 
 * Interface reprenant les méthodes devant être implémentées dans les Web Services
 * BACK, mais en y ajoutant les mappings REST de Spring
 * 
 * @author qdeme
 * @author fgaujous
 *
 */
public abstract class AbstractAfApiController implements AfApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAfApiController.class);

    @RequestMapping(value = "/demandes/{demandeId}/annuler", method = RequestMethod.PUT)
    public void annulerDemandeRequest(@PathVariable(value = "demandeId") Integer demandeId,
            @RequestParam(value = "usagerId", required = true) Integer usagerId) {
        LOGGER.info("AbstractAfApiController.annulerDemande(" + demandeId + "," + usagerId + ")");
        annulerDemande(demandeId, usagerId);
    }

    @RequestMapping(value = "/demandes", method = RequestMethod.POST)
    public DemandeDTO creerDemandeRequest(@Valid @RequestBody DemandeInputDTO demande,
            @RequestParam(value = "usagerId", required = true) Integer usagerId) {
        LOGGER.info("AbstractAfApiController.creerDemande(" + demande + "," + usagerId + ")");
        return creerDemande(demande, usagerId);
    }

    @RequestMapping(value = "/demandes/{demandeId}/complements/{icId}", method = RequestMethod.PUT)
    public DemandeComplementsDTO repondreDemandeComplementsRequest(@PathVariable(value = "demandeId") Integer demandeId,
            @PathVariable(value = "icId") Integer icId, @Valid @RequestBody DemandeComplementsReponseDTO reponse) {
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
    public DemandeDTO associerDemandeCourrierRequest(@RequestParam(value = "identifiantDemande", required = true) String identifiantDemande,
            @RequestParam(value = "nomProprio", required = true) String nomProprio,
            @RequestParam(value = "usagerId", required = true) Integer usagerId) {
        LOGGER.info("AbstractAfApiController.associerDemandeCourrierRequest(" + identifiantDemande + "," + nomProprio + "," + usagerId + ")");
        return associerDemandeCourrier(identifiantDemande, nomProprio, usagerId);
    }

    @RequestMapping(value = "/accesses/{usagerId}", method = RequestMethod.DELETE)
    public void desinscriptionUsagerRequest(@PathVariable(value = "usagerId") Integer usagerId,
            @RequestParam(value = "hashedPassword", required = true) String hashedPassword) {
        LOGGER.info("AbstractAfApiController.desinscriptionUsagerRequest(" + usagerId + " (+hashedPassword))");
        desinscriptionUsager(usagerId, hashedPassword);
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
    public UsagerCourrierDTO getUsagerCourrierRequest(@PathVariable(value = "usagerCourrierId") Integer usagerCourrierId) {
        LOGGER.info("AbstractAfApiController.getUsagerCourrierRequest(" + usagerCourrierId + ")");
        return getUsagerCourrier(usagerCourrierId);
    }
    
    @RequestMapping(value = "/motifs", method = RequestMethod.GET)
    public List<MotifDTO> getMotifsRequest() {
        LOGGER.info("AbstractAfApiController.getMotifsRequest()");
        return getMotifs();
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

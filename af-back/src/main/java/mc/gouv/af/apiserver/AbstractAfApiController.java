package mc.gouv.af.apiserver;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import mc.gouv.dem.apishared.model.DemandeComplementsDTO;
import mc.gouv.dem.apishared.model.DemandeComplementsReponseDTO;
import mc.gouv.dem.apishared.model.DemandeDTO;
import mc.gouv.dem.apishared.model.DemandeInputDTO;

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
    public void annulerDemandeRequest(@PathVariable(value="demandeId") Integer demandeId,
            @RequestParam(value="usagerId", required=true) Integer usagerId) {
        LOGGER.info("AbstractAfApiController.annulerDemande(" + demandeId + "," + usagerId + ")");
        annulerDemande(demandeId, usagerId);
    }
    
    @RequestMapping(value = "/demandes", method = RequestMethod.POST)
    public DemandeDTO creerDemandeRequest(@Valid @RequestBody DemandeInputDTO demande,
            @RequestParam(value="usagerId", required=true) Integer usagerId) {
        LOGGER.info("AbstractAfApiController.creerDemande(" + demande + "," + usagerId + ")");
        return creerDemande(demande, usagerId);
    }
    
    @RequestMapping(value = "/demandes/{demandeId}/complements/{icId}", method = RequestMethod.PUT)
    public DemandeComplementsDTO repondreDemandeComplementsRequest(@PathVariable(value="demandeId") Integer demandeId,
            @PathVariable(value="icId") Integer icId,
            @Valid @RequestBody DemandeComplementsReponseDTO reponse) {
        LOGGER.info("AbstractAfApiController.repondreDemandeComplements(" + demandeId + "," + icId + "," + reponse + ")");
        return repondreDemandeComplements(demandeId, icId, reponse);
    }
    
    @RequestMapping(value = "/demandes/{demandeId}", method = RequestMethod.GET)
    public @ResponseBody DemandeDTO getDemandeRequest(@PathVariable(value = "demandeId") Integer demandeId) {
        LOGGER.info("AbstractAfApiController.getDemande(" + demandeId + ")");
        return getDemande(demandeId);
    }

    @RequestMapping(value = "/demandes", method = RequestMethod.GET)
    public @ResponseBody List<DemandeDTO> getDemandesRequest(@RequestParam(value="usagerId", required=true) Integer usagerId) {
        LOGGER.info("AbstractAfApiController.getDemandes(" + usagerId + ")");
        return getDemandes(usagerId);
    }

    @RequestMapping(value = "/demandes/{demandeId}/complements", method = RequestMethod.GET)
    public @ResponseBody List<DemandeComplementsDTO> getDemandeComplementsRequest(@PathVariable(value = "demandeId") Integer demandeId) {
        LOGGER.info("AbstractAfApiController.getDemandeComplements(" + demandeId + ")");
        return getDemandeComplements(demandeId);
    }

    @RequestMapping(value = "/demandes/{demandeId}/complements/{icId}", method = RequestMethod.GET)
    public @ResponseBody DemandeComplementsDTO getDemandeComplementsRequest(@PathVariable(value = "demandeId") Integer demandeId,
            @PathVariable(value = "icId") Integer icId) {
        LOGGER.info("AbstractAfApiController.getDemandeComplements(" + demandeId + "," + icId + ")");
        return getDemandeComplements(demandeId, icId);
    }

}

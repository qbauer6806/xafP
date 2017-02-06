package mc.gouv.af.apiserver;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import mc.gouv.dem.apishared.model.DemandeDTO;

@RequestMapping(value = "/api/v1/hab", produces = "application/json")
public abstract class AbstractAfApiController implements AfApiController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAfApiController.class);
    
    @RequestMapping(value = "/{demandeId}", method = RequestMethod.GET)
    public List<DemandeDTO> getDemandesRequest(@PathVariable(value="demandeId") Integer demandeId) {
        LOGGER.info("Test demandeId=" + demandeId);
        return getDemandes();
    }

}

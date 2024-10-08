package mc.gouv.xaf.backweb.ws;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import mc.gouv.xaf.back.service.DemandeRecapHTMLService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 *
 * @author qdeme
 *
 */
@GouvRestController
@RequestMapping(value = "/ws/recap")
public class RecapGenerationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecapGenerationController.class);

	@Autowired
	private DemandesService demandesService;

    @Autowired
	private DemandeRecapHTMLService demandeRecapHTMLService;

	@Secured({"ROLE_LECTURE"})
	@GetMapping(value = "/{pkDemande}", produces = "text/html")
	public @ResponseBody String getRecap(@PathVariable(value = "pkDemande") Integer pkDemande) throws IOException, ParseException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		LOGGER.info("======================= Appel de /ws/recap/{}", pkDemande);
		DemandeDTO demande = demandesService.getDemande(pkDemande);
		String ret = "";
		if (demande != null) {
			ret = getHTML(demande);
		}
		LOGGER.info("======================= Fin appel de /ws/recap/{}", pkDemande);
		return ret;
	}

	public String getHTML(DemandeDTO demande)
			throws IOException, ParseException, ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return demandeRecapHTMLService.getHTMLDemandeContenuRecap(demande, false);
	}
}

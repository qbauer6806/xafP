package mc.gouv.af.backweb.ws;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.af.back.service.DemandeRecapHTMLService;
import mc.gouv.dem.service.DemandesService;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.xboot.config.web.annotation.GouvRestController;

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
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Autowired
	private DemandeRecapHTMLService demandeRecapHTMLService;

	@RequestMapping(value = "/{pkDemande}", method = RequestMethod.GET, produces = "text/html")
	public @ResponseBody String getRecap(@PathVariable(value = "pkDemande") Integer pkDemande) throws Exception {

		LOGGER.info("======================= Appel de /ws/recap/" + pkDemande);

		DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), pkDemande);

		String ret = "";

		if (demande != null) {
			ret = getHTML(demande);
		}

		LOGGER.info("======================= Fin appel de /ws/recap/" + pkDemande);

		return ret;

	}

	public String getHTML(DemandeDTO demande)
			throws IOException, ParseException, ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return demandeRecapHTMLService.getHTMLDemandeContenuRecap(demande);
	}
}

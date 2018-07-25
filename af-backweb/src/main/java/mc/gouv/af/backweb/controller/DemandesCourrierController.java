package mc.gouv.af.backweb.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;

import mc.gouv.af.back.cache.UsagersCache;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.af.backweb.formbean.DemandesCourrierFormBean;
import mc.gouv.af.backweb.formbean.UsagerCourrierFormBean;
import mc.gouv.dem.shared.model.DemandeCanalEnum;
import mc.gouv.servicerest.usager.model.UsagerBean;

/**
 * Controller pour les demandes courrier
 * 
 * @author qdeme
 *
 */
@Controller
@RequestMapping("/demandes/courriers")
public class DemandesCourrierController extends AbstractController {

	@Autowired
	private UsagersCache usagersCache;

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	private static final Logger LOGGER = LoggerFactory.getLogger(DemandesCourrierController.class);

	@Secured("ROLE_TRAITEMENT")
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView form(@ModelAttribute("usagerCourrierFormBean") UsagerCourrierFormBean usagerCourrierFormBean)
			throws JsonProcessingException {

		LOGGER.info("======================= Appel de la page /demandes/courriers");

		ModelAndView mav = new ModelAndView("demandes/demandescourrier");

		LOGGER.info("======================= Fin /demandes/courriers");

		return mav;
	}

	@Secured("ROLE_TRAITEMENT")
	@RequestMapping(value = "/creer/{usagerId}", method = RequestMethod.POST)
	public ModelAndView creerDemandeCourrier(@PathVariable(value = "usagerId") Integer usagerId,
			@Valid @ModelAttribute("demandesCourrierFormBean") DemandesCourrierFormBean demandesCourrierFormBean,
			BindingResult bindingResult) throws Exception {

		ModelAndView mav;
		LOGGER.info("======================= Appel de la page POST /demandes/courriers/creer/{usagerId}");

		if (bindingResult.hasErrors()) {
			mav = new ModelAndView("demandes/demandescourrier2");
			mav = initForm(mav, usagerId);
			List<String> errors = new ArrayList<String>();
			errors.add(AfBackUtils.MESSAGE_ERREURS_FORMULAIRE);
			mav.addObject("errors", errors);
			return mav;
		}

		// Conversion de la date au format iso
		// #6366
		SimpleDateFormat dt1 = new SimpleDateFormat("dd/MM/yyyy");
		Date dateReception = dt1.parse(demandesCourrierFormBean.getDateReception());
		SimpleDateFormat dateReceptionIsoFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dateReceptionIso = dateReceptionIsoFormat.format(dateReception);

		String id = "c_" + demandesCourrierFormBean.getUsagerId();
		Date currentDate = new Date();
		long currentMilli = currentDate.getTime();
		String sig = DigestUtils.sha256Hex(gouvPropertiesResolver.getFrontSharedKey() + id + currentMilli) + ":"
				+ currentMilli;

		// c_ pour que AfServlet sache qu'il s'agit d'un usager courrier et
		// qu'il faut appeler DEM à la place de Login

		mav = new ModelAndView("redirect:" + gouvPropertiesResolver.getFrontUrl() + "acces_teleservice.html?id=" + id
				+ "&international=fr&canal=" + demandesCourrierFormBean.getCanal() + "&langue="
				+ demandesCourrierFormBean.getLangue() + "&courrierDateReception=" + dateReceptionIso
				+ "&courrierRefInterne=" + demandesCourrierFormBean.getRefInterne() + "&target=/"
				+ gouvPropertiesResolver.getFrontFormStartPage() + "&creeParAgentId="
				+ AfBackUtils.getAuthenticatedAgentId() + "&sig=" + sig);

		LOGGER.info("======================= Fin /demandes/courriers/redirFront");

		return mav;
	}

	@Secured("ROLE_TRAITEMENT")
	@RequestMapping(value = "/creer/{usagerId}", method = RequestMethod.GET)
	public ModelAndView form(@PathVariable(value = "usagerId") Integer usagerId,
			@ModelAttribute("demandesCourrierFormBean") DemandesCourrierFormBean demandesCourrierFormBean)
			throws JsonProcessingException {

		LOGGER.info("======================= Appel de la page /demandes/courriers/creer/" + usagerId);

		ModelAndView mav = new ModelAndView("demandes/demandescourrier2");

		mav = initForm(mav, usagerId);

		LOGGER.info("======================= Fin /demandes/courriers/creer");

		return mav;
	}

	private ModelAndView initForm(ModelAndView mav, Integer usagerId) {

		UsagerBean usagerCourrier = usagersCache.get(usagerId);
		mav.addObject("usager", StringUtils.trim(StringUtils.defaultString(usagerCourrier.getPrenom()) + " "
				+ StringUtils.defaultString(usagerCourrier.getNom())));

		ArrayList<DemandeCanalEnum> canaux = new ArrayList<DemandeCanalEnum>();
		canaux.add(DemandeCanalEnum.COURRIER);
		canaux.add(DemandeCanalEnum.GUICHET_PHYSIQUE);
		mav.addObject("canaux", canaux);
		return mav;
	}

}

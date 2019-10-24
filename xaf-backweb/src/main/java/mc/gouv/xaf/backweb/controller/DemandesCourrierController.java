package mc.gouv.xaf.backweb.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import mc.gouv.xaf.back.service.utils.UtilisateursUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.utils.URIBuilder;
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

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.shared.dto.DemandeCanalEnum;
import mc.gouv.xaf.backweb.formbean.DemandesCourrierFormBean;
import mc.gouv.xaf.backweb.formbean.UsagerCourrierFormBean;

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
	private DemarchesDataProvider demarchesDataProvider;

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Autowired
	private UtilisateursUtils utilisateursUtils;

	private static final Logger LOGGER = LoggerFactory.getLogger(DemandesCourrierController.class);

	@Secured({"ROLE_TRAITEMENT","ROLE_SAISIE"})
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView form(@ModelAttribute("usagerCourrierFormBean") UsagerCourrierFormBean usagerCourrierFormBean)
			throws JsonProcessingException {

		LOGGER.info("======================= Appel de la page /demandes/courriers");

		ModelAndView mav = new ModelAndView("demandes/demandescourrier");

		LOGGER.info("======================= Fin /demandes/courriers");

		return mav;
	}

	@Secured({"ROLE_TRAITEMENT","ROLE_SAISIE"})
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
		
		// Récupérer des properties s'il faut ordonner au Front de désactiver la validation des champs du formulaire
		String novalidate = "";
		if (gouvPropertiesResolver.getNovalidate()) {
		    novalidate = "&novalidate=true";
		}

		URIBuilder ub = new URIBuilder(gouvPropertiesResolver.getFrontUrl() + "acces_teleservice.html");
		ub.addParameter("id", id);
		ub.addParameter("international", "fr"+novalidate);
		ub.addParameter("canal", demandesCourrierFormBean.getCanal());
		ub.addParameter("langue", demandesCourrierFormBean.getLangue());
		ub.addParameter("courrierDateReception", dateReceptionIso);
		ub.addParameter("courrierRefInterne", demandesCourrierFormBean.getRefInterne());
		ub.addParameter("target", "/" + gouvPropertiesResolver.getFrontFormStartPage());
		ub.addParameter("creeParAgentId", AfBackUtils.getAuthenticatedAgentId());
		ub.addParameter("sig", sig);

		String redirect = "redirect:" + ub;
		
		LOGGER.info("URL de redirection vers le front : " + redirect);

		mav = new ModelAndView(redirect);

		LOGGER.info("======================= Fin /demandes/courriers/redirFront");

		return mav;
	}

	@Secured({"ROLE_TRAITEMENT","ROLE_SAISIE"})
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

		mav.addObject("usager", utilisateursUtils.getUsagerCourrierFromId(usagerId));

		ArrayList<DemandeCanalEnum> canaux = new ArrayList<DemandeCanalEnum>();
		canaux.add(DemandeCanalEnum.COURRIER);
		canaux.add(DemandeCanalEnum.GUICHET_PHYSIQUE);
		mav.addObject("canaux", canaux);
		mav.addObject("langues", demarchesDataProvider.getLanguesDisponibles());
		return mav;
	}

}

package mc.gouv.xaf.backweb.controller;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.UsagersCourrierService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.UsagersUtils;
import mc.gouv.xaf.backweb.formbean.DemandesCourrierFormBean;
import mc.gouv.xaf.backweb.formbean.UsagerCourrierFormBean;
import mc.gouv.xaf.shared.dto.DemandeCanalEnum;
import mc.gouv.xaf.shared.dto.DemandeDTO;

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
    private UsagersUtils usagersUtils;

    @Autowired
    private UsagersCourrierService usagersCourrierService;

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesCourrierController.class);

    @Secured("ROLE_SAISIE")
    @GetMapping
    public ModelAndView form(@ModelAttribute("usagerCourrierFormBean") UsagerCourrierFormBean usagerCourrierFormBean) {
        LOGGER.info("======================= Appel de la page /demandes/courriers");
        ModelAndView mav = new ModelAndView("demandes/demandescourrier");
        LOGGER.info("======================= Fin /demandes/courriers");
        return mav;
    }

    @Secured({ "ROLE_TRAITEMENT", "ROLE_SAISIE" })
    @PostMapping(value = "/creer/{usagerId}")
    public ModelAndView creerDemandeCourrier(@PathVariable(value = "usagerId") Integer usagerId,
            @Valid @ModelAttribute("demandesCourrierFormBean") DemandesCourrierFormBean demandesCourrierFormBean,
            BindingResult bindingResult) throws URISyntaxException, ParseException {

        ModelAndView mav;
        LOGGER.info("======================= Appel de la page POST /demandes/courriers/creer/{}", usagerId);

        if (bindingResult.hasErrors()) {
            mav = new ModelAndView("demandes/demandescourrier2");
            initForm(mav, usagerId);
            List<String> errors = new ArrayList<>();
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
        ub.addParameter("international", "fr" + novalidate);
        ub.addParameter("canal", demandesCourrierFormBean.getCanal());
        ub.addParameter("langue", demandesCourrierFormBean.getLangue());
        ub.addParameter("courrierDateReception", dateReceptionIso);
        ub.addParameter("courrierRefInterne", demandesCourrierFormBean.getRefInterne());
        ub.addParameter("target", "/" + gouvPropertiesResolver.getFrontFormStartPage());
        ub.addParameter("creeParAgentId", AfBackUtils.getAuthenticatedAgentId());
        ub.addParameter("sig", sig);
        if (demandesCourrierFormBean.getDuplicationKeyId() != null)
            ub.addParameter("duplicationKeyId", demandesCourrierFormBean.getDuplicationKeyId());

        String redirect = "redirect:" + ub;

        LOGGER.info("URL de redirection vers le front : {}", redirect);

        mav = new ModelAndView(redirect);

        LOGGER.info("======================= Fin /demandes/courriers/creer/{}", usagerId);

        return mav;
    }

    @Secured({ "ROLE_TRAITEMENT", "ROLE_SAISIE" })
    @GetMapping(value = "/creer/{usagerId}")
    public ModelAndView form(@PathVariable(value = "usagerId") Integer usagerId,
            @ModelAttribute("demandesCourrierFormBean") DemandesCourrierFormBean demandesCourrierFormBean) {

        LOGGER.info("======================= Appel de la page /demandes/courriers/creer/{}", usagerId);

        /* recuperation de la derniere demande pour duplication */
        DemandeDTO derniereDemande = usagersCourrierService.getDerniereDemandePourDuplication(
                gouvPropertiesResolver.getDemarcheId(), usagerId, demarchesDataProvider.getStatutsPourDuplication());

        ModelAndView mav = new ModelAndView("demandes/demandescourrier2");
        if (derniereDemande != null) {
            mav.addObject("duplicationKeyId", derniereDemande.getPkDemandes());
            mav.addObject("duplicationIdentifiant", derniereDemande.getIdentifiant());
        }
        initForm(mav, usagerId);
        LOGGER.info("======================= Fin /demandes/courriers/creer/{}", usagerId);
        return mav;
    }

    private void initForm(ModelAndView mav, Integer usagerId) {
        mav.addObject("usager", usagersUtils.getUsagerCourrierFromId(usagerId));
        ArrayList<DemandeCanalEnum> canaux = new ArrayList<>();
        canaux.add(DemandeCanalEnum.COURRIER);
        canaux.add(DemandeCanalEnum.GUICHET_PHYSIQUE);
        mav.addObject("canaux", canaux);
        mav.addObject("langues", demarchesDataProvider.getLanguesDisponibles());
    }
}

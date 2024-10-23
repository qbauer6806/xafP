package mc.gouv.xaf.backweb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mc.gouv.servicerest.pays.model.PaysBean;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.UsagersCourrierService;
import mc.gouv.xaf.back.service.itg.rest.PaysCache;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.PaysComparator;
import mc.gouv.xaf.back.service.utils.UsagersUtils;
import mc.gouv.xaf.backweb.dto.UsagerCourrierResultDTO;
import mc.gouv.xaf.backweb.formbean.TransfertDemandesFormBean;
import mc.gouv.xaf.backweb.formbean.UsagerCourrierFormBean;
import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller pour la page /gestionusagers
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/gestion/usagers")
public class GestionUsagersController extends AbstractController {

    private static final String REDIRECT_USAGER = "redirect:/gestion/usagers";
    private static final String CREATION_USAGER_URL = "gestion/usagers/creationusagercourrier";

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionUsagersController.class);

    private static final String I18N_CREATION_USAGER_COURRIER_SUCCESS_CODE_MESSAGE = "message.success.creation.usager.courrier";
    private static final String I18N_MODIFICATION_USAGER_COURRIER_SUCCESS_CODE_MESSAGE = "message.success.modification.usager.courrier";
    private static final String I18N_SUPPRESSION_USAGER_COURRIER_SUCCESS_CODE_MESSAGE = "message.success.suppression.usager.courrier";
    private static final String I18N_TRANSFERT_DEMANDES_ERROR_CODE_MESSAGE = "message.error.transfert.demandes";
    private static final String I18N_TRANSFERT_DEMANDES_USAGER_COURRIER_SUCCESS_CODE_MESSAGE = "message.success.transfert.demandes.usager.courrier";
    private static final String I18N_TRANSFERTSUPPRESSION_DEMANDES_USAGER_COURRIER_SUCCESS_CODE_MESSAGE = "message.success.transfertsuppression.demandes.usager.courrier";

    @Autowired
    private PaysCache paysCache;

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private GouvBPM gouvBPM;

    @Autowired
    private UsagersCourrierService usagersCourrierService;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private UsagersCache usagersCache;

    /**
     * Affichage de la page principale
     */
    @Secured({ "ROLE_TRAITEMENT", "ROLE_SAISIE" })
    @GetMapping
    public ModelAndView form(@RequestParam(name = "usagers", required = false) List<UsagerCourrierResultDTO> usagers) {

        LOGGER.info("======================= Appel de la page /gestionusagers");

        if (usagers == null) {
            LOGGER.info("Appel à DEM pour récupérer la liste des usagers courrier...");
            List<UsagerCourrierDTO> usagersCourrierDTO = usagersCourrierService.getUsagersCourrier(null);

            usagers = usagersDemToDemarche(usagersCourrierDTO);
        }

        LOGGER.info("======================= Fin /gestionusagers");

        ModelAndView mav = new ModelAndView("gestion/usagers/gestionusagers");
        mav.addObject("usagers", usagers);
        return mav;
    }

    /**
     * Bugfis
     */
    @Secured({ "ROLE_TRAITEMENT", "ROLE_SAISIE" })
    @GetMapping(value = "/rechercher")
    public ModelAndView test() {
        return new ModelAndView("redirect:");
    }

    /**
     * Recherche d'un usager courrier depuis la page principale (POST)
     */
    @Secured({ "ROLE_TRAITEMENT", "ROLE_SAISIE" })
    @PostMapping(value = "/rechercher")
    public ModelAndView print(@RequestParam String usagerInput) {
        String safeUsager = AfBackUtils.logSafe(usagerInput);
        LOGGER.info("======================= Appel de la page /gestion/usagers/rechercher ({})", safeUsager);

        LOGGER.info("Appel à DEM pour rechercher l'usager courrier...");
        List<UsagerCourrierDTO> usagersCourrierDTO = usagersCourrierService.getUsagersCourrier(usagerInput);

        List<UsagerCourrierResultDTO> usagers = usagersDemToDemarche(usagersCourrierDTO);

        ModelAndView mav = new ModelAndView("gestion/usagers/gestionusagers");
        mav.addObject("usagers", usagers);
        mav.addObject("usagerInput", usagerInput);

        LOGGER.info("======================= Fin /gestion/usagers/rechercher");

        return mav;
    }

    /**
     * Affichage du formulaire de création d'usager courrier (GET)
     */
    @Secured({ "ROLE_TRAITEMENT", "ROLE_SAISIE" })
    @GetMapping(value = "/creer")
    public ModelAndView form(@ModelAttribute("usagerCourrierFormBean") UsagerCourrierFormBean usagerCourrierFormBean) {

        LOGGER.info("======================= Appel de la page /gestion/usagers/creer (GET)");

        ModelAndView mav = new ModelAndView(CREATION_USAGER_URL);

        LOGGER.info("======================= Fin /gestion/usagers/creer (GET)");

        return ajouterListesPays(mav);
    }

    /**
     * Création de l'usager courrier depuis le formulaire de création (POST)
     */
    @Secured({ "ROLE_TRAITEMENT", "ROLE_SAISIE" })
    @PostMapping(value = "/creer")
    public ModelAndView creerUsagerCourrier(
            @Valid @ModelAttribute("usagerCourrierFormBean") UsagerCourrierFormBean usagerCourrierFormBean,
            BindingResult bindingResult, @RequestParam(required = false) Integer updateUsagerId,
            final RedirectAttributes redirectAttributes) {

        ModelAndView mav;
        LOGGER.info("======================= Appel de la page /gestion/usagers/creer (POST)");

        UsagerCourrierDTO usagerCourrier = new UsagerCourrierDTO();
        usagerCourrier.setAdresse1(usagerCourrierFormBean.getAdresse1());
        usagerCourrier.setAdresse2(usagerCourrierFormBean.getAdresse2());
        usagerCourrier.setAdresseComplement(usagerCourrierFormBean.getAdresseComplement());
        usagerCourrier.setCodePostal(usagerCourrierFormBean.getCodePostal());
        usagerCourrier.setEmail(usagerCourrierFormBean.getEmail());
        usagerCourrier.setNom(usagerCourrierFormBean.getNom());
        usagerCourrier.setPrenom(usagerCourrierFormBean.getPrenom());
        usagerCourrier.setRaisonSociale(usagerCourrierFormBean.getRaisonSociale());
        usagerCourrier.setTelephone(usagerCourrierFormBean.getTelephone());
        usagerCourrier.setVille(usagerCourrierFormBean.getVille());
        usagerCourrier.setTitre(usagerCourrierFormBean.getTitre());
        usagerCourrier.setPays(usagerCourrierFormBean.getPaysChoisi());

        if (StringUtils.isBlank(usagerCourrierFormBean.getNom()) && StringUtils.isBlank(
                usagerCourrierFormBean.getRaisonSociale())) {
            FieldError fe1 = new FieldError("usagerCourrierFormBean", "nom",
                    "Au moins le nom OU la raison sociale doivent être renseignés");
            FieldError fe2 = new FieldError("usagerCourrierFormBean", "raisonSociale",
                    "Au moins le nom OU la raison sociale doivent être renseignés");
            bindingResult.addError(fe1);
            bindingResult.addError(fe2);
        }

        if (bindingResult.hasErrors()) {
            mav = new ModelAndView(CREATION_USAGER_URL);
            List<String> errors = new ArrayList<>();
            errors.add(AfBackUtils.MESSAGE_ERREURS_FORMULAIRE);
            mav.addObject("errors", errors);
            if (updateUsagerId != null) {
                mav.addObject("updateUsagerId", updateUsagerId);
            }
            return ajouterListesPays(mav);
        }

        // Création du contenu de l'accès qui devra être créé pour cet usager
        // courrier
        // En l'occurrence : acceptation des CGU
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jNodeCGU = mapper.createObjectNode();
        ObjectNode jNodeValue = mapper.createObjectNode();
        jNodeCGU.set("CGU", jNodeValue.booleanNode(true));
        usagerCourrier.setAccessContenu(jNodeCGU);

        if (updateUsagerId == null) {
            LOGGER.info("Appel à DEM pour création de l'usager...");
            usagerCourrier = usagersCourrierService.saveUsagerCourrier(usagerCourrier);

            // Ajout du message de succès
            List<String> messages = new ArrayList<>();
            messages.add(
                    messageSource.getMessage(I18N_CREATION_USAGER_COURRIER_SUCCESS_CODE_MESSAGE, null, Locale.FRENCH));
            redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);
        } else {
            LOGGER.info("Appel à DEM pour mise à jour de l'usager {}...", updateUsagerId);
            usagerCourrier.setLogin(updateUsagerId.toString());
            usagerCourrier.setPkUsagersCourrier(updateUsagerId);
            usagerCourrier = usagersCourrierService.updateUsagerCourrier(usagerCourrier);

            // Ajout du message de succès
            List<String> messages = new ArrayList<>();
            messages.add(messageSource.getMessage(I18N_MODIFICATION_USAGER_COURRIER_SUCCESS_CODE_MESSAGE, null,
                    Locale.FRENCH));
            redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);
        }

        // Update du cache
        GichuniUsagerDTO usagerCourrierBean = UsagersUtils.convertUsagerCourrierDTOToGichuniUsagerDTO(usagerCourrier);
        usagersCache.add(usagerCourrierBean.getId(), usagerCourrierBean);

        LOGGER.info("======================= Fin /gestion/usagers/creer (POST)");

        return new ModelAndView("redirect:" + usagerCourrier.getPkUsagersCourrier());
    }

    /**
     * Affichage de la page d'un usager courrier
     */
    @Secured({ "ROLE_TRAITEMENT", "ROLE_SAISIE" })
    @GetMapping(value = "/{usagerId}")
    public ModelAndView visualiserUsager(@PathVariable(value = "usagerId") Integer usagerId,
            @ModelAttribute("usagerCourrierFormBean") UsagerCourrierFormBean usagerCourrierFormBean) {

        LOGGER.info("======================= Appel de la page /gestion/usagers/{}", usagerId);

        UsagerCourrierDTO usager = usagersCourrierService.getUsagerCourrier(usagerId);
        usagerCourrierFormBean.setAdresse1(usager.getAdresse1());
        usagerCourrierFormBean.setAdresse2(usager.getAdresse2());
        usagerCourrierFormBean.setAdresseComplement(usager.getAdresseComplement());
        usagerCourrierFormBean.setCodePostal(usager.getCodePostal());
        usagerCourrierFormBean.setEmail(usager.getEmail());
        usagerCourrierFormBean.setNom(usager.getNom());
        usagerCourrierFormBean.setPaysChoisi(usager.getPays());
        usagerCourrierFormBean.setPrenom(usager.getPrenom());
        usagerCourrierFormBean.setRaisonSociale(usager.getRaisonSociale());
        usagerCourrierFormBean.setTelephone(usager.getTelephone());
        usagerCourrierFormBean.setTitre(usager.getTitre());
        usagerCourrierFormBean.setVille(usager.getVille());

        LOGGER.info("======================= Fin /gestion/usagers");

        ModelAndView mav = new ModelAndView(CREATION_USAGER_URL);
        mav.addObject("updateUsagerId", usagerId);
        return ajouterListesPays(mav);
    }

    /**
     * Suppression d'un usager courrier depuis la page principale (POST)
     */
    @Secured({ "ROLE_TRAITEMENT", "ROLE_SAISIE" })
    @PostMapping(value = "/supprimer")
    public ModelAndView supprimer(@RequestParam Integer usagerId, final RedirectAttributes redirectAttributes) {

        LOGGER.info("======================= Appel de la page /gestion/usagers/supprimer ({})", usagerId);
        LOGGER.info("Appel à DEM pour supprimer l'usager courrier...");
        usagersCourrierService.deleteUsagerCourrier(usagerId);

        usagersCache.refresh();

        // TODO gestion des erreurs ?

        ModelAndView mav = new ModelAndView(REDIRECT_USAGER);

        // Ajout du message de succès
        List<String> messages = new ArrayList<>();
        messages.add(
                messageSource.getMessage(I18N_SUPPRESSION_USAGER_COURRIER_SUCCESS_CODE_MESSAGE, null, Locale.FRENCH));
        redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);

        LOGGER.info("======================= Fin /gestion/usagers/supprimer");

        return mav;
    }

    /**
     * Transfert de demandes tout court Permettre de sélectionner/déselectionner les demandes
     */
    @Secured({ "ROLE_TRAITEMENT", "ROLE_SAISIE" })
    @GetMapping(value = "/transferer/{usagerSourceId}/{usagerCibleId}")
    public ModelAndView transferer(@PathVariable(value = "usagerSourceId") Integer usagerSourceId,
            @PathVariable(value = "usagerCibleId") Integer usagerCibleId, final RedirectAttributes redirectAttributes) {

        LOGGER.info("======================= Appel de la page /gestion/usagers/transferer ({}, {})", usagerSourceId,
                usagerCibleId);

        ModelAndView mav = transfererGeneric(usagerSourceId, usagerCibleId, redirectAttributes);

        mav.addObject("transfererSupprimer", false);

        LOGGER.info("======================= Fin /gestion/usagers/transferer");

        return mav;
    }

    /**
     * Transfert de demandes suivi de la suppression de l'usager Ne pas permettre la désélection des demandes
     */
    @Secured({ "ROLE_TRAITEMENT", "ROLE_SAISIE" })
    @GetMapping(value = "/transferersupprimer/{usagerSourceId}/{usagerCibleId}")
    public ModelAndView transfererSupprimer(@PathVariable(value = "usagerSourceId") Integer usagerSourceId,
            @PathVariable(value = "usagerCibleId") Integer usagerCibleId, final RedirectAttributes redirectAttributes) {

        LOGGER.info("======================= Appel de la page /gestion/usagers/transfererSupprimer ({}, {})",
                usagerSourceId, usagerCibleId);

        ModelAndView mav = transfererGeneric(usagerSourceId, usagerCibleId, redirectAttributes);

        mav.addObject("transfererSupprimer", true);

        LOGGER.info("======================= Fin /gestion/usagers/transfererSupprimer");

        return mav;
    }

    private ModelAndView transfererGeneric(Integer usagerSourceId, Integer usagerCibleId,
            final RedirectAttributes redirectAttributes) {

        if (usagerSourceId.equals(usagerCibleId)) {
            // L'usager cible doit être différent de l'usager source
            List<String> messages = new ArrayList<>();
            messages.add(messageSource.getMessage(I18N_TRANSFERT_DEMANDES_ERROR_CODE_MESSAGE, null, Locale.FRENCH));
            redirectAttributes.addFlashAttribute("errorMessages", messages);

            return new ModelAndView(REDIRECT_USAGER);
        }

        LOGGER.info("Appel à DEM pour récupérer la liste des demandes effectuées par l'usager source...");
        List<DemandeDTO> demandes = demandesService.getDemandes(usagerSourceId);

        LOGGER.info("Appel à DEM afin de récupérer les infos de l'usager source...");
        UsagerCourrierDTO usagerSourceDTO = usagersCourrierService.getUsagerCourrier(usagerSourceId);

        ModelAndView mav = new ModelAndView("gestion/usagers/transfertdemandes");
        mav.addObject("usagerSourceId", usagerSourceId);
        mav.addObject("usagerCibleId", usagerCibleId);
        mav.addObject("demandes", demandes);
        mav.addObject("usagerSourceDTO", usagerSourceDTO);

        return mav;
    }

    @ModelAttribute(value = "transfertDemandesFormBean")
    public TransfertDemandesFormBean transfertDemandesFormBean() {
        return new TransfertDemandesFormBean();
    }

    /**
     * Transfert de demandes tout court ou suivi de la suppression de l'usager courrier
     */
    @Secured({ "ROLE_TRAITEMENT", "ROLE_SAISIE" })
    @PostMapping(value = "/transferer", params = "action=Valider")
    public ModelAndView transfererPost(
            @ModelAttribute("transfertDemandesFormBean") TransfertDemandesFormBean transfertDemandesFormBean,
            @RequestParam Integer usagerSourceId, @RequestParam Integer usagerCibleId,
            @RequestParam boolean transfererSupprimer, final RedirectAttributes redirectAttributes) {

        LOGGER.info("======================= Appel de la page /gestion/usagers/transferer (POST)");

        LOGGER.info("Appel à DEM pour récupérer les demandes affectées à l'usager courrier source...");
        List<DemandeDTO> demandes = demandesService.getDemandes(usagerSourceId);

        LOGGER.info(
                "Appel à DEM pour transférer les demandes de l'usager courrier source {} vers l'usager courrier cible {} ({})... ",
                usagerSourceId, usagerCibleId, transfertDemandesFormBean.getCheckedDemandes());
        usagersCourrierService.transferer(usagerSourceId, usagerCibleId,
                transfertDemandesFormBean.getCheckedDemandes());

        LOGGER.info("Mise à jour des variables BPM concernant les demandes impactées...");
        for (DemandeDTO demande : demandes) {
            gouvBPM.setProcessBusinessVariable(demande.getPkDemandes(),
                    GouvBPMProcessVariableTypeEnum.MC_USAGERID.name(), usagerCibleId);
        }

        if (transfererSupprimer) {

            LOGGER.info("Appel à DEM pour supprimer l'usager courrier source {}...", usagerSourceId);
            usagersCourrierService.deleteUsagerCourrier(usagerSourceId);

            List<String> messages = new ArrayList<>();
            messages.add(
                    messageSource.getMessage(I18N_TRANSFERTSUPPRESSION_DEMANDES_USAGER_COURRIER_SUCCESS_CODE_MESSAGE,
                            new Object[] { afBackUtils.getUsagerNameFromID(usagerSourceId),
                                    afBackUtils.getUsagerNameFromID(usagerCibleId) }, Locale.FRENCH));
            redirectAttributes.addFlashAttribute("successMessages", messages);
        } else {
            List<String> messages = new ArrayList<>();
            messages.add(messageSource.getMessage(I18N_TRANSFERT_DEMANDES_USAGER_COURRIER_SUCCESS_CODE_MESSAGE,
                    new Object[] { afBackUtils.getUsagerNameFromID(usagerSourceId),
                            afBackUtils.getUsagerNameFromID(usagerCibleId) }, Locale.FRENCH));
            redirectAttributes.addFlashAttribute("successMessages", messages);
        }

        LOGGER.info("======================= Fin /gestion/usagers/transferer (POST)");

        return new ModelAndView(REDIRECT_USAGER);
    }

    private List<UsagerCourrierResultDTO> usagersDemToDemarche(List<UsagerCourrierDTO> uDems) {
        List<UsagerCourrierResultDTO> uDemarches = new ArrayList<>();
        for (UsagerCourrierDTO uDem : uDems) {
            uDemarches.add(usagerDemToDemarche(uDem));
        }
        return uDemarches;
    }

    private UsagerCourrierResultDTO usagerDemToDemarche(UsagerCourrierDTO uDem) {
        UsagerCourrierResultDTO uDemarche = new UsagerCourrierResultDTO();
        if (StringUtils.isBlank(uDem.getNom()) && StringUtils.isBlank(uDem.getPrenom())) {
            uDemarche.setNomRaisonSociale(uDem.getRaisonSociale());
        } else if (!StringUtils.isBlank(uDem.getRaisonSociale())) {
            uDemarche.setNomRaisonSociale(
                    uDem.getNom() + " " + uDem.getPrenom() + " (" + uDem.getRaisonSociale() + ")");
        } else {
            uDemarche.setNomRaisonSociale(uDem.getNom() + " " + uDem.getPrenom());
        }
        uDemarche.setAdresse(uDem.getAdresse1());
        uDemarche.setNbDemandes(uDem.getNbDemandes());
        uDemarche.setUsagerId(uDem.getPkUsagersCourrier());
        uDemarche.setNomPrenom(uDem.getNom() + " " + uDem.getPrenom());
        uDemarche.setCodePostal(uDem.getCodePostal());
        uDemarche.setVille(uDem.getVille());
        uDemarche.setRaisonSociale(uDem.getRaisonSociale());

        return uDemarche;
    }

    private ModelAndView ajouterListesPays(ModelAndView mav) {
        // Trier les pays en fonction de leur priorité
        ArrayList<PaysBean> listePaysP1 = new ArrayList<>();
        ArrayList<PaysBean> listePaysP2 = new ArrayList<>();
        ArrayList<PaysBean> listePaysP3 = new ArrayList<>();
        ArrayList<PaysBean> listePaysP4 = new ArrayList<>();
        for (PaysBean p : paysCache.getAll().values()) {
            if (p.getPriorite() == 1) {
                listePaysP1.add(p);
            } else if (p.getPriorite() == 2) {
                listePaysP2.add(p);
            } else if (p.getPriorite() == 3) {
                listePaysP3.add(p);
            } else if (p.getPriorite() == 4) {
                listePaysP4.add(p);
            }
        }
        PaysComparator paysComparator = new PaysComparator();
        listePaysP1.sort(paysComparator);
        listePaysP2.sort(paysComparator);
        listePaysP3.sort(paysComparator);
        listePaysP4.sort(paysComparator);
        mav.addObject("listePaysP1", listePaysP1);
        mav.addObject("listePaysP2", listePaysP2);
        mav.addObject("listePaysP3", listePaysP3);
        mav.addObject("listePaysP4", listePaysP4);
        return mav;
    }

}

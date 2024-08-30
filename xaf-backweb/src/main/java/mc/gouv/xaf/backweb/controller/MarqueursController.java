package mc.gouv.xaf.backweb.controller;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import jakarta.transaction.Transactional;
import mc.gouv.xaf.back.service.data.DemandesConfigService;
import mc.gouv.xaf.back.service.data.MarqueursService;
import mc.gouv.xaf.backweb.dto.VersionModeleDTO;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.MarqueurDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller pour la page /marqueurs
 *
 * @author mpavone
 */
@Controller
@RequestMapping("/gestion/marqueurs")
//@Secured("ROLE_LECTURE")
public class MarqueursController extends AbstractController {

    private static final String AJOUTER_SUCCES = "Le marqueur a été ajouté.";
    private static final String SUPPRIMER_SUCCES = "Le marqueur a été supprimé.";
    private static final String MODIFIER_SUCCES = "Le marqueur a été modifié.";

    private static final String REDIRECT = "redirect:/gestion/marqueurs";

    private static final Logger LOGGER = LoggerFactory.getLogger(MarqueursController.class);

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");

    @Autowired
    private DemandesConfigService demandesConfigService;

    @Autowired
    private MarqueursService marqueursService;

    @Secured({ "ROLE_PARAMETRAGE", "ROLE_CONFIGURATION" })
    @GetMapping
    public ModelAndView form(final RedirectAttributes redirectAttributes) {
        return loadMarqueurs(null);
    }

    private ModelAndView loadMarqueurs(String buildId){
        LOGGER.info("======================= Appel de la page /marqueurs");

        ModelAndView mav = new ModelAndView("gestion/marqueurs/marqueurs");

        List<String> buildIds = demandesConfigService.getBuildIds();

        List<VersionModeleDTO> versionModeleDTOS = new ArrayList<>();
        for (String b : buildIds) {
            VersionModeleDTO versionModeleDTO = new VersionModeleDTO();
            versionModeleDTO.setBuildId(b);
            ZonedDateTime dateTime = Instant.ofEpochMilli(Long.parseLong(b)).atZone(ZoneId.systemDefault());
            versionModeleDTO.setDate(dateTime.format(formatter));
            versionModeleDTOS.add(versionModeleDTO);
        }

        String currentBuildId = buildId != null ? buildId : buildIds.get(0);

        List<String> chemins = demandesConfigService.getModelPathsRechercheAvancee(currentBuildId);
        List<MarqueurDTO> marqueurs = marqueursService.getMarqueurs(currentBuildId);

        mav.addObject("marqueurs", marqueurs);
        mav.addObject("chemins", chemins);
        mav.addObject("versions", versionModeleDTOS);
        mav.addObject("buildId", currentBuildId);

        LOGGER.info("======================= Fin /marqueurs");

        return mav;
    }

    @GetMapping(value = "/{buildId}")
    public ModelAndView form(@PathVariable(value = "buildId") String buildId) {
        return loadMarqueurs(buildId);
    }

    @PostMapping(value = "/edit", params = "action=ajouter")
    @Transactional
    public ModelAndView ajouter(@RequestParam String description, @RequestParam String identifiant,
            @RequestParam String chemin, @RequestParam String buildId, final RedirectAttributes redirectAttributes) {

        MarqueurDTO marqueur = new MarqueurDTO();
        marqueur.setDescription(description);
        marqueur.setIdentifiant(identifiant);
        marqueur.setChemin(chemin);
        marqueur.setBuildId(buildId);
        marqueursService.saveOrUpdateMarqueur(marqueur);
        return redirectSuccess(redirectAttributes, AJOUTER_SUCCES, buildId);
    }

    @PostMapping(value = "/supprimer")
    @Transactional
    public ModelAndView supprimer(@RequestParam Integer pkMarqueur, @RequestParam String buildId, final RedirectAttributes redirectAttributes) {
        marqueursService.deleteMarqueur(pkMarqueur);
        return redirectSuccess(redirectAttributes, SUPPRIMER_SUCCES, buildId);
    }

    @PostMapping(value = "/edit", params = "action=modifier")
    @Transactional
    public ModelAndView modifier(@RequestParam String description, @RequestParam String identifiant,
            @RequestParam String chemin, @RequestParam Integer pkMarqueur, @RequestParam String buildId,
            final RedirectAttributes redirectAttributes) {

        MarqueurDTO marqueurDTO = new MarqueurDTO();
        marqueurDTO.setPkMarqueur(pkMarqueur);
        marqueurDTO.setDescription(description);
        marqueurDTO.setIdentifiant(identifiant);
        marqueurDTO.setChemin(chemin);
        marqueurDTO.setBuildId(buildId);
        marqueursService.saveOrUpdateMarqueur(marqueurDTO);
        return redirectSuccess(redirectAttributes, MODIFIER_SUCCES, buildId);
    }

    private ModelAndView redirectSuccess(RedirectAttributes redirectAttributes, String message, String buildId) {
        ModelAndView mav = new ModelAndView(REDIRECT + "/" + buildId);
        List<String> messages = new ArrayList<>();
        messages.add(message);
        redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);
        return mav;
    }

}

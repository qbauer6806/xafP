package mc.gouv.xaf.backweb.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.service.data.DemandesConfigService;
import mc.gouv.xaf.back.service.data.MarqueursService;
import mc.gouv.xaf.backweb.dto.ConfigDTO;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.MarqueurDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller pour la page /marqueurs
 *
 * @author mpavone
 */
@Controller
@RequestMapping("/gestion/marqueurs")
@Secured("ROLE_CONFIGURATION")
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

    @GetMapping
    public ModelAndView form(final RedirectAttributes redirectAttributes) {
        return loadMarqueurs(null);
    }

    private ModelAndView loadMarqueurs(String buildId){
        LOGGER.info("======================= Appel de la page /marqueurs");

        ModelAndView mav = new ModelAndView("gestion/marqueurs/marqueurs");
        List<DemandeConfigBO> configs = demandesConfigService.getConfigsBO();

        List<ConfigDTO> configDTOS = new ArrayList<>();
        for (DemandeConfigBO config : configs) {
            ConfigDTO configDTO = new ConfigDTO();
            configDTO.setBuildId(config.getBuildId());
            ZonedDateTime dateTime = Instant.ofEpochMilli(Long.parseLong(config.getBuildId())).atZone(ZoneId.systemDefault());
            configDTO.setDate(dateTime.format(formatter));
            configDTO.setVersion(config.getVersion() != null ? config.getVersion() : "");
            configDTOS.add(configDTO);
        }

        String currentBuildId = buildId != null ? buildId : configs.getFirst().getBuildId();

        List<String> chemins = demandesConfigService.getModelPathsRechercheAvancee(currentBuildId);
        List<MarqueurDTO> marqueurs = marqueursService.getMarqueurs(currentBuildId);

        mav.addObject("marqueurs", marqueurs);
        mav.addObject("chemins", chemins);
        mav.addObject("configs", configDTOS);
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
        String complementUrl = buildId != null ? "/" + buildId : "";
        ModelAndView mav = new ModelAndView(REDIRECT + complementUrl);
        List<String> messages = new ArrayList<>();
        messages.add(message);
        redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);
        return mav;
    }

    private ModelAndView redirectError(RedirectAttributes redirectAttributes, String message) {
        ModelAndView mav = new ModelAndView(REDIRECT);
        List<String> messages = new ArrayList<>();
        messages.add(message);
        redirectAttributes.addFlashAttribute(SharedMessages.ERROR_MESSAGES, messages);
        return mav;
    }

    @GetMapping(path = "/export")
    public ResponseEntity<InputStreamResource> exportConfig(HttpServletRequest request) throws IOException {
        String jsonFile = marqueursService.exportConfig();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=marqueurs-config-"
                + new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss").format(new Date()) + ".json");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        responseHeaders.add("Content-Transfer-Encoding", "binary");

        InputStreamResource isr = new InputStreamResource(
                new ByteArrayInputStream(jsonFile.getBytes(StandardCharsets.UTF_8)));

        return ResponseEntity.ok().headers(responseHeaders).body(isr);
    }

    @PostMapping("/import")
    public ModelAndView handleFileUpload(@RequestParam("file") MultipartFile file, final RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            return redirectError(redirectAttributes, "Aucun fichier sélectionné");
        } else {
            try {
                marqueursService.importConfig(file.getBytes());
                return redirectSuccess(redirectAttributes, "L'import a été correctement effectué", null);
            } catch (IOException e) {
                LOGGER.error("Erreur lors de l'import", e);
                return redirectError(redirectAttributes, "Un problème est survenu lors de l'import");
            }
        }

    }

}

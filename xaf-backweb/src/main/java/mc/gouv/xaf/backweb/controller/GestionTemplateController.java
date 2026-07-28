package mc.gouv.xaf.backweb.controller;

import jakarta.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.data.MarqueursService;
import mc.gouv.xaf.back.service.data.TemplatesService;
import mc.gouv.xaf.back.service.data.impl.DemandesConfigHelperService;
import mc.gouv.xaf.back.service.itg.mail.impl.AfMailTemplateModelProvider;
import mc.gouv.xaf.back.service.templates.GestionTemplateService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.MarqueurDTO;
import mc.gouv.xaf.shared.dto.TemplateDTO;
import mc.gouv.xaf.shared.formbean.TemplateCreateFormBean;
import mc.gouv.xaf.shared.formbean.TemplateFormBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

/**
 * Controller pour la modification de templates d'e-mails
 *
 * @author mpavone
 */
@Controller
@Secured({ "ROLE_CONFIGURATION" })
@RequestMapping("/gestion/template")
@RequiredArgsConstructor
public class GestionTemplateController extends AbstractController {

    private final GestionTemplateService gestionTemplateService;
    private final TemplatesService templatesService;
    private final AfMailTemplateModelProvider afMailTemplateModelProvider;
    private final MarqueursService marqueursService;
    private final DemandesConfigHelperService demandesConfigHelperService;
    private final DemandesService demandesService;
    private final DemarchesService demarchesService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionTemplateController.class);
    private static final String FR_ONLY_VAR = "frOnly";
    private static final String VARIABLES_GLOBALES_VAR = "variablesGlobales";
    private static final String MARQUEURS_VAR = "marqueurs";

    // Messages
    private static final String MESSAGE_SUCCESS_MODIFICATION = "Le template mail a été modifié avec succès";

    @GetMapping
    public ModelAndView getTemplates() {
        LOGGER.info("Appel de la page /gestion/template. Méthode getTemplates");
        ModelAndView mav = new ModelAndView("gestion/template/template");
        boolean frOnly = isFrenchOnly();
        mav.addObject(FR_ONLY_VAR, frOnly);
        mav.addObject("pkDemandeTest", demandesService.getDerniereDemande().orElse(new DemandeDTO()).getPkDemandes());
        List<TemplateDTO> templateList = frOnly ? templatesService.getTemplates("fr") : templatesService.getTemplates();
        mav.addObject("templateList", templateList);
        LOGGER.info("======================= Fin /gestion/template. Méthode getTemplates");
        return mav;
    }

    @GetMapping(path = "/update")
    public ModelAndView formUpdateInit(@ModelAttribute("templateFormBean") TemplateFormBean templateFormBean) {
        LOGGER.info("Appel de la page /gestion/template/templateupdate. Méthode formUpdateInit");
        ModelAndView mav = new ModelAndView("gestion/template/templateupdate");
        mav.addObject(FR_ONLY_VAR, isFrenchOnly());
        mav.addObject(VARIABLES_GLOBALES_VAR, getVariablesGlobales());
        mav.addObject(MARQUEURS_VAR, getMarqueursList());
        templateFormBean.setPkDemandeTest(
                demandesService.getDerniereDemande().orElse(new DemandeDTO()).getPkDemandes());
        gestionTemplateService.retrieveTemplateForm(templateFormBean);
        LOGGER.info("======================= Fin /gestion/template/templateupdate. Méthode formUpdateInit");
        return mav;
    }

    @PostMapping(path = "/update")
    public ModelAndView traiterUpdate(@Valid @ModelAttribute("templateFormBean") TemplateFormBean templateFormBean,
            BindingResult bindingResult, RedirectAttributes ra) {
        LOGGER.info("Appel de la page /gestion/template/update. Méthode traiterUpdate");

        if (bindingResult.hasErrors()) {
            List<String> erreurs = bindingResult.getAllErrors().stream().map(ObjectError::getDefaultMessage).toList();
            ra.addFlashAttribute(SharedMessages.ERROR_MESSAGES, erreurs);
            return new ModelAndView(
                    "redirect:/gestion/template/update?code=" + UriUtils.encode(templateFormBean.getCode(),
                            StandardCharsets.UTF_8) + "&langue=" + templateFormBean.getLangue());
        }

        gestionTemplateService.saveTemplateForm(templateFormBean);
        ra.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, Collections.singletonList(MESSAGE_SUCCESS_MODIFICATION));

        return new ModelAndView("redirect:/gestion/template");
    }

    @GetMapping(path = "/create")
    public ModelAndView formCreateSave(
            @ModelAttribute("templateCreateFormBean") TemplateCreateFormBean templateCreateFormBean) {
        LOGGER.info("Appel de la page /gestion/template/templatecreate. Méthode formCreateSave");
        ModelAndView mav = new ModelAndView("gestion/template/templatecreate");
        mav.addObject(FR_ONLY_VAR, isFrenchOnly());
        mav.addObject(VARIABLES_GLOBALES_VAR, getVariablesGlobales());
        mav.addObject(MARQUEURS_VAR, getMarqueursList());
        templateCreateFormBean.setPkDemandeTest(
                demandesService.getDerniereDemande().orElse(new DemandeDTO()).getPkDemandes());
        LOGGER.info("======================= Fin /gestion/template/templatecreate. Méthode formCreateSave");
        return mav;
    }

    @PostMapping(path = "/create")
    public ModelAndView formCreateInit(
            @Valid @ModelAttribute("templateCreateFormBean") TemplateCreateFormBean templateCreateFormBean,
            BindingResult bindingResult, final RedirectAttributes ra) {
        LOGGER.info("Appel de la page /gestion/template/create. Méthode formCreateInit");

        if (bindingResult.hasErrors()) {
            List<String> erreurs = bindingResult.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .toList();
            ra.addFlashAttribute(SharedMessages.ERROR_MESSAGES, erreurs);
            return new ModelAndView("redirect:/gestion/template/create");
        }

        try {
            gestionTemplateService.saveTemplateForm(templateCreateFormBean);
        } catch (Exception e) {
            ra.addFlashAttribute(SharedMessages.ERROR_MESSAGES, Collections.singletonList(e.getMessage()));
            return new ModelAndView("redirect:/gestion/template/create");
        }

        ra.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, Collections.singletonList("Le template mail a été créé avec succès"));

        LOGGER.info("======================= Fin /gestion/template/create. Méthode formCreateInit");

        return new ModelAndView("redirect:/gestion/template");
    }

    @GetMapping(path = "/export-templates")
    public ResponseEntity<InputStreamResource> exportTemplates() throws IOException {

        LOGGER.info("Appel /export-templates. Méthode exportTemplates");

        String jsonFile = gestionTemplateService.exportConfig();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=export-templates-" + new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss").format(
                        new Date()) + ".json");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        responseHeaders.add("Content-Transfer-Encoding", "binary");

        InputStreamResource isr = new InputStreamResource(
                new ByteArrayInputStream(jsonFile.getBytes(StandardCharsets.UTF_8)));

        LOGGER.info("======================= Fin /export-templates. Méthode exportTemplates");

        return ResponseEntity.ok().headers(responseHeaders).body(isr);
    }

    @PostMapping(path = "/import-templates")
    public ModelAndView importConfig(@RequestParam("file") MultipartFile file, final RedirectAttributes ra)
            throws IOException {

        LOGGER.info("Appel /import-templates");
        gestionTemplateService.importConfig(file.getBytes());
        ra.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES,
                Collections.singletonList("L'import a été effectué avec succès"));

        return new ModelAndView("redirect:/gestion/template");
    }

    @DeleteMapping(path = "/{langue}/{templateCode}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String langue, @PathVariable String templateCode) {
        LOGGER.info("Appel /deleteTemplate");

        gestionTemplateService.deleteTemplate(templateCode, langue);

        LOGGER.info("======================= Fin /deleteTemplate. Le template {} a été supprimé avec succès",
                AfBackUtils.logSafe(templateCode));
        return ResponseEntity.ok().build();
    }

    private boolean isFrenchOnly() {
        // S'il n'y a qu'une langue on ne récupère que les templates FR
        Map<String, String> langues = demarchesService.getLanguesDisponibles();
        return langues.size() == 1 && langues.containsKey("fr");
    }

    private Map<String, Object> getVariablesGlobales() {
        Optional<DemandeDTO> demandeOpt = demandesService.getDerniereDemande();
        if (demandeOpt.isEmpty()) {
            return Collections.emptyMap();
        }
        return afMailTemplateModelProvider.getModel(null, "", demandeOpt.get(), null, null, null);
    }

    private List<MarqueurDTO> getMarqueursList() {
        DemandeConfigBO config = demandesConfigHelperService.getLastConfig();
        List<MarqueurDTO> marqueurs = new ArrayList<>();
        if (config != null && StringUtils.isNotBlank(config.getBuildId())) {
            marqueurs = marqueursService.getMarqueurs(config.getBuildId());
        }
        return marqueurs;
    }
}

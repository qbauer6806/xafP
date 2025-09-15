package mc.gouv.xaf.backweb.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import feign.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.TemplatesService;
import mc.gouv.xaf.back.service.templates.GestionTemplateService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.TemplateDTO;
import mc.gouv.xaf.shared.formbean.TemplateFormBean;

/**
 * Controller pour la modification de templates d'e-mails
 *
 * @author mpavone
 */
@Controller
@Secured({ "ROLE_CONFIGURATION" })
@RequestMapping("/gestion/template")
public class GestionTemplateController extends AbstractController {

    @Autowired
    private GestionTemplateService gestionTemplateService;

    @Autowired
    private BackGouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private TemplatesService templatesService;

    @Autowired
    private AfBackUtils afBackUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionTemplateController.class);
    private static final String TS_CODE_VAR = "tsCode";
    private static final String FR_ONLY_VAR = "frOnly";

    // Messages
    private static final String MESSAGE_SUCCESS_MODIFICATION = "Le template mail a été modifié avec succès";

    @GetMapping
    public ModelAndView getTemplates() {
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        LOGGER.info("Appel de la page /gestion/template. Méthode getTemplates");
        ModelAndView mav = new ModelAndView("gestion/template/template");
        boolean frOnly = isFrenchOnly();
        mav.addObject(TS_CODE_VAR, demarcheId);
        mav.addObject(FR_ONLY_VAR, frOnly);
        List<TemplateDTO> templateList = frOnly ? templatesService.getTemplates("fr") : templatesService.getTemplates();
        mav.addObject("templateList", templateList);
        LOGGER.info("======================= Fin /gestion/template. Méthode getTemplates");
        return mav;
    }

    @GetMapping(path = "/update")
    public ModelAndView formUpdateInit(@ModelAttribute("templateFormBean") TemplateFormBean templateFormBean) {
        LOGGER.info("Appel de la page /gestion/template/update. Méthode formUpdateInit");
        ModelAndView mav = new ModelAndView("gestion/template/templateupdate");
        mav.addObject(TS_CODE_VAR, gouvPropertiesResolver.getDemarcheId());
        mav.addObject(FR_ONLY_VAR, isFrenchOnly());
        gestionTemplateService.retrieveTemplateForm(templateFormBean);
        LOGGER.info("======================= Fin /gestion/template/update. Méthode formUpdateInit");
        return mav;
    }

    @PostMapping(path = "/update")
    public ModelAndView traiterUpdate(@ModelAttribute("templateFormBean") TemplateFormBean templateFormBean) {
        LOGGER.info("Appel de la page /gestion/template/update. Méthode traiterUpdate");
        ModelAndView mav = new ModelAndView("gestion/template/templateupdate");
        mav.addObject(TS_CODE_VAR, gouvPropertiesResolver.getDemarcheId());
        gestionTemplateService.saveTemplateForm(templateFormBean);
        mav.addObject(FR_ONLY_VAR, isFrenchOnly());
        // Récupération à nouveau du template pour vérifier que tout est ok
        gestionTemplateService.retrieveTemplateForm(templateFormBean);
        mav.addObject(SharedMessages.SUCCESS_MESSAGES, MESSAGE_SUCCESS_MODIFICATION);
        LOGGER.info("======================= Fin /gestion/template/update. Méthode traiterUpdate");
        return mav;
    }

    @GetMapping(path = "/export-templates")
    public ResponseEntity<InputStreamResource> exportTemplates() throws IOException {

        LOGGER.info("Appel /export-templates. Méthode exportTemplates");

        String jsonFile = templatesService.exportConfig();
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
    public ModelAndView importConfig(@RequestParam("file") MultipartFile file) throws IOException {
        LOGGER.info("Appel /import-templates");
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        templatesService.importConfig(file.getBytes());

        ModelAndView mav = new ModelAndView("redirect:/gestion/template");
        boolean frOnly = isFrenchOnly();
        mav.addObject(TS_CODE_VAR, demarcheId);
        mav.addObject(FR_ONLY_VAR, frOnly);
        List<TemplateDTO> templateList = frOnly ? templatesService.getTemplates("fr") : templatesService.getTemplates();
        mav.addObject("templateList", templateList);

        LOGGER.info("======================= Fin /import-templates. La configuration a été importée avec succès");

        return mav;
    }

    private boolean isFrenchOnly() {
        // S'il n'y a qu'une langue on ne récupère que les templates FR
        Map<String, String> langues = afBackUtils.getLanguesDisponibles();
        return langues.size() == 1 && langues.containsKey("fr");
    }
}

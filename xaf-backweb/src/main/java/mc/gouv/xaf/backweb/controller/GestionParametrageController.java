package mc.gouv.xaf.backweb.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.data.impl.DemandesConfigHelperService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.backweb.formbean.ParametrageFormBean;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemarcheDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/gestion/parametrage")
@Secured("ROLE_CONFIGURATION")
@RequiredArgsConstructor
public class GestionParametrageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionParametrageController.class);

    private static final String REDIRECT = "redirect:/gestion/parametrage";

    private final DemarchesService demarchesService;
    private final DemandesConfigHelperService demandesConfigHelperService;
    private final AfBackUtils afBackUtils;

    @Value("${display.name}")
    private String displayName;

    @Value("${maven.version}")
    private String mavenVersion;

    @Value("${xaf.version}")
    private String xafVersion;

    @GetMapping
    public ModelAndView form(@ModelAttribute("parametrageFormBean") ParametrageFormBean parametrageFormBean,
            final RedirectAttributes redirectAttributes) {
        LOGGER.info("Appel de la page /gestion/parametrage. Méthode form");
        ModelAndView mav = new ModelAndView("gestion/parametrage/parametrage");
        if (afBackUtils.getDemarcheCanHandleProperties()) {
            DemarcheDTO demarche = afBackUtils.getDemarcheInfos();
            parametrageFormBean.setNomDemarche(demarche.getNom());
            parametrageFormBean.setEmailFrom(demarche.getEmailFrom());
            parametrageFormBean.setEmailFromNom(demarche.getEmailFromNom());
            parametrageFormBean.setEmailReplyto(demarche.getEmailReplyto());
            parametrageFormBean.setEmailReplytoNom(demarche.getEmailReplytoNom());
            parametrageFormBean.setEmailService(demarche.getEmailService());
            parametrageFormBean.setIdentifiantPrefixe(demarche.getIdentifiantPrefixe());

            parametrageFormBean.setNomDirection(demarche.getNomDirection());
            parametrageFormBean.setNomSousDirection(demarche.getNomSousDirection());
            parametrageFormBean.setNomFooter(demarche.getNomFooter());
            parametrageFormBean.setAdresseService(demarche.getAdresseService());
            parametrageFormBean.setNomSousDirectionComplement(demarche.getNomSousDirectionComplement());
            parametrageFormBean.setTelephoneService(demarche.getTelephoneService());
            parametrageFormBean.setNomDemarcheEn(demarche.getNomEn());
            parametrageFormBean.setNomDirectionEn(demarche.getNomDirectionEn());
            parametrageFormBean.setNomSousDirectionEn(demarche.getNomSousDirectionEn());
            parametrageFormBean.setNomSousDirectionComplementEn(demarche.getNomSousDirectionComplementEn());
        }

        mav.addObject("displayName", displayName);
        mav.addObject("xafVersion", xafVersion);
        DemandeConfigBO config = demandesConfigHelperService.getLastConfig();
        JsonNode node = config.getContenu().get("wysiwygVersion");
        String wysiwygVersion = node != null ? node.asText() : null;
        mav.addObject("wysiwygVersion", wysiwygVersion);
        mav.addObject("tsVersion", mavenVersion);

        LOGGER.info("======================= Fin /gestion/parametrage. Méthode form");
        return mav;
    }

    /**
     * Création de l'usager courrier depuis le formulaire de création (POST)
     */
    @Secured({ "ROLE_CONFIGURATION" })
    @PostMapping(value = "/sauvegarder", params = "action=Sauvegarder")
    public ModelAndView sauvegarderParametrage(
            @Valid @ModelAttribute("parametrageFormBean") ParametrageFormBean parametrageFormBean,
            BindingResult bindingResult, final RedirectAttributes redirectAttributes) {

        ModelAndView mav;
        LOGGER.info("======================= Appel de la page /gestion/parametrage/save (POST)");

        mav = new ModelAndView(REDIRECT);

        DemarcheDTO demarche = afBackUtils.getDemarcheInfos();
        demarche.setNom(parametrageFormBean.getNomDemarche());
        demarche.setEmailFrom(parametrageFormBean.getEmailFrom());
        demarche.setEmailFromNom(parametrageFormBean.getEmailFromNom());
        demarche.setEmailReplyto(parametrageFormBean.getEmailReplyto());
        demarche.setEmailReplytoNom(parametrageFormBean.getEmailReplytoNom());
        demarche.setEmailService(parametrageFormBean.getEmailService());
        // remove space from identifiantPrefixe
        demarche.setIdentifiantPrefixe(StringUtils.deleteWhitespace(parametrageFormBean.getIdentifiantPrefixe()));
        demarche.setNomDirection(parametrageFormBean.getNomDirection());
        demarche.setNomSousDirection(parametrageFormBean.getNomSousDirection());
        demarche.setNomFooter(parametrageFormBean.getNomFooter());
        demarche.setAdresseService(parametrageFormBean.getAdresseService());
        demarche.setNomSousDirectionComplement(parametrageFormBean.getNomSousDirectionComplement());
        demarche.setTelephoneService(parametrageFormBean.getTelephoneService());
        demarche.setNomEn(parametrageFormBean.getNomDemarcheEn());
        demarche.setNomDirectionEn(parametrageFormBean.getNomDirectionEn());
        demarche.setNomSousDirectionEn(parametrageFormBean.getNomSousDirectionEn());
        demarche.setNomSousDirectionComplementEn(parametrageFormBean.getNomSousDirectionComplementEn());

        demarchesService.updateDemarche(demarche);
        List<String> messages = new ArrayList<>();
        messages.add("Vous venez de modifier la démarche avec succès.");
        redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);

        LOGGER.info("======================= Fin /gestion/parametrage/save (POST)");

        return mav;
    }

    private ModelAndView redirectError(RedirectAttributes redirectAttributes, String message) {
        ModelAndView mav = new ModelAndView(REDIRECT);
        List<String> messages = new ArrayList<>();
        messages.add(message);
        redirectAttributes.addFlashAttribute(SharedMessages.ERROR_MESSAGES, messages);
        return mav;
    }

    private ModelAndView redirectSuccess(RedirectAttributes redirectAttributes, String message) {
        ModelAndView mav = new ModelAndView(REDIRECT);
        List<String> messages = new ArrayList<>();
        messages.add(message);
        redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);
        return mav;
    }

    @GetMapping(path = "/export")
    public ResponseEntity<InputStreamResource> exportConfig(HttpServletRequest request) throws IOException {
        String jsonFile = demarchesService.exportConfig();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=parametrage-config-" + new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss").format(
                        new Date()) + ".json");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        responseHeaders.add("Content-Transfer-Encoding", "binary");

        InputStreamResource isr = new InputStreamResource(
                new ByteArrayInputStream(jsonFile.getBytes(StandardCharsets.UTF_8)));

        return ResponseEntity.ok().headers(responseHeaders).body(isr);
    }

    @PostMapping("/import")
    public ModelAndView handleFileUpload(@RequestParam("file") MultipartFile file,
            final RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            return redirectError(redirectAttributes, "Aucun fichier sélectionné");
        } else {
            // Logique d'importation du fichier JSON
            try {
                demarchesService.importConfig(file.getBytes());
                return redirectSuccess(redirectAttributes, "L'import a été correctement effectué");
            } catch (IOException e) {
                LOGGER.error("Erreur lors de l'import", e);
                return redirectError(redirectAttributes, "Un problème est survenu lors de l'import");
            }
        }

    }

}

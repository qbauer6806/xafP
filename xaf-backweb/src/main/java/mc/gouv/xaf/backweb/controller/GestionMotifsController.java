package mc.gouv.xaf.backweb.controller;

import jakarta.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.data.MotifsService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.formbean.MotifCreateFormBean;
import mc.gouv.xaf.shared.formbean.MotifFormBean;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

@Controller
@Secured({ "ROLE_CONFIGURATION" })
@RequestMapping("/gestion/motifs")
@RequiredArgsConstructor
public class GestionMotifsController extends AbstractController {

    private final MotifsService motifsService;
    private final DemandesService demandesService;
    private final DemarchesService demarchesService;
    private final DemarchesDataProvider demarchesDataProvider;

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionMotifsController.class);
    private static final String FR_ONLY_VAR = "frOnly";
    private static final String STATUTS_VAR = "statuts";

    // Messages
    private static final String MESSAGE_SUCCESS_MODIFICATION = "Le motif a été modifié avec succès";

    @GetMapping
    public ModelAndView getMotifs() {
        LOGGER.info("Appel de la page /gestion/motifs. Méthode getMotifs");
        ModelAndView mav = new ModelAndView("gestion/motifs/motifs");
        boolean frOnly = isFrenchOnly();
        mav.addObject(FR_ONLY_VAR, frOnly);
        mav.addObject("pkDemandeTest", demandesService.getDerniereDemande().orElse(new DemandeDTO()).getPkDemandes());
        List<MotifDTO> motifList = frOnly ? motifsService.getMotifs("fr") : motifsService.getMotifs();
        mav.addObject("motifList", motifList);
        LOGGER.info("======================= Fin /gestion/motifs. Méthode getMotifs");
        return mav;
    }

    @GetMapping(path = "/update")
    public ModelAndView formUpdateInit(@ModelAttribute("motifFormBean") MotifFormBean motifFormBean) {
        LOGGER.info("Appel de la page /gestion/motifs/motifupdate. Méthode formUpdateInit");
        ModelAndView mav = new ModelAndView("gestion/motifs/motifupdate");
        mav.addObject(FR_ONLY_VAR, isFrenchOnly());
        mav.addObject(STATUTS_VAR, getStatutsCibles());

        try {
            MotifDTO motifDTO = motifsService.getMotif(motifFormBean.getCode(), motifFormBean.getLangue());
            motifFormBean.setCommentairePrerempli(motifDTO.getCommentairePrerempli());
            motifFormBean.setTexteAEnvoyer(motifDTO.getTexteAEnvoyer());
            motifFormBean.setLibelle(motifDTO.getLibelle());
            motifFormBean.setStatut(motifDTO.getStatut());
        } catch (Exception e) {
            LOGGER.error("Aucun motif trouvé pour le code {}", motifFormBean.getCode());
        }

        LOGGER.info("======================= Fin /gestion/motifs/motifupdate. Méthode formUpdateInit");
        return mav;
    }

    private Map<String, String> getStatutsCibles() {
        Map<String, String> statuts = new LinkedHashMap<>(demarchesDataProvider.getStatusMap());
        statuts.remove(demarchesDataProvider.getPremierStatutCreationDemande());
        return statuts;
    }

    @PostMapping(path = "/update")
    public ModelAndView traiterUpdate(@Valid @ModelAttribute("motifFormBean") MotifFormBean motifFormBean,
            BindingResult bindingResult, RedirectAttributes ra) {
        LOGGER.info("Appel de la page /gestion/motifs/update. Méthode traiterUpdate");

        if (bindingResult.hasErrors()) {
            List<String> erreurs = bindingResult.getAllErrors().stream().map(ObjectError::getDefaultMessage).toList();
            ra.addFlashAttribute(SharedMessages.ERROR_MESSAGES, erreurs);
            return new ModelAndView("redirect:/gestion/motifs/update?code=" + UriUtils.encode(motifFormBean.getCode(),
                    StandardCharsets.UTF_8) + "&langue=" + motifFormBean.getLangue());
        }

        try {
            MotifDTO motifDTO = motifsService.getMotif(motifFormBean.getCode(), motifFormBean.getLangue());
            motifDTO.setLibelle(motifFormBean.getLibelle());
            motifDTO.setStatut(motifFormBean.getStatut());
            motifDTO.setCommentairePrerempli(motifFormBean.getCommentairePrerempli());
            motifDTO.setTexteAEnvoyer(motifFormBean.getTexteAEnvoyer());
            motifsService.saveOrUpdateMotif(motifDTO);
        } catch (Exception e) {
            LOGGER.error("Aucun motif trouvé pour le code {}", motifFormBean.getCode(), e);
        }
        ra.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, Collections.singletonList(MESSAGE_SUCCESS_MODIFICATION));
        return new ModelAndView("redirect:/gestion/motifs");
    }

    @GetMapping(path = "/create")
    public ModelAndView formCreateSave(@ModelAttribute("motifCreateFormBean") MotifCreateFormBean motifCreateFormBean) {
        LOGGER.info("Appel de la page /gestion/motifs/motifcreate. Méthode formCreateSave");
        ModelAndView mav = new ModelAndView("gestion/motifs/motifcreate");
        mav.addObject(FR_ONLY_VAR, isFrenchOnly());
        mav.addObject(STATUTS_VAR, getStatutsCibles());
        LOGGER.info("======================= Fin /gestion/motifs/motifcreate. Méthode formCreateSave");
        return mav;
    }

    @PostMapping(path = "/create")
    public ModelAndView formCreateInit(
            @Valid @ModelAttribute("motifCreateFormBean") MotifCreateFormBean motifCreateFormBean,
            BindingResult bindingResult, final RedirectAttributes ra) {
        LOGGER.info("Appel de la page /gestion/template/create. Méthode formCreateInit");

        if (bindingResult.hasErrors()) {
            List<String> erreurs = bindingResult.getAllErrors().stream().map(ObjectError::getDefaultMessage).toList();
            ra.addFlashAttribute(SharedMessages.ERROR_MESSAGES, erreurs);
            return new ModelAndView("redirect:/gestion/motifs/create");
        }

        try {
            motifsService.saveMotifForm(motifCreateFormBean);
        } catch (Exception e) {
            ra.addFlashAttribute(SharedMessages.ERROR_MESSAGES, Collections.singletonList(e.getMessage()));
            return new ModelAndView("redirect:/gestion/motifs/create");
        }

        ra.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES,
                Collections.singletonList("Le motif a été créé avec succès"));

        LOGGER.info("======================= Fin /gestion/template/create. Méthode formCreateInit");

        return new ModelAndView("redirect:/gestion/motifs");
    }

    @GetMapping(path = "/export-motifs")
    public ResponseEntity<InputStreamResource> exportMotifs() throws IOException {

        LOGGER.info("Appel /export-motifs. Méthode exportMotifs");

        String jsonFile = motifsService.exportConfig();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=export-motifs-" + new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss").format(
                        new Date()) + ".json");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        responseHeaders.add("Content-Transfer-Encoding", "binary");

        InputStreamResource isr = new InputStreamResource(
                new ByteArrayInputStream(jsonFile.getBytes(StandardCharsets.UTF_8)));

        LOGGER.info("======================= Fin /export-motifs. Méthode exportMotifs");

        return ResponseEntity.ok().headers(responseHeaders).body(isr);
    }

    @PostMapping(path = "/import-motifs")
    public ModelAndView importConfig(@RequestParam("file") MultipartFile file, final RedirectAttributes ra)
            throws IOException {

        LOGGER.info("Appel /import-motifs");
        motifsService.importConfig(file.getBytes());
        ra.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES,
                Collections.singletonList("L'import a été effectué avec succès"));

        return new ModelAndView("redirect:/gestion/motifs");
    }

    @DeleteMapping(path = "/desactiver/{langue}/{motifCode}")
    public ResponseEntity<MotifDTO> desactiverMotif(@PathVariable String langue, @PathVariable String motifCode) {

        LOGGER.info("Appel /desactiverMotif");

        MotifDTO motifDto = motifsService.desactiverMotif(motifCode, langue);

        LOGGER.info("======================= Fin /desactiverMotif. Le motif {} a été supprimé avec succès",
                AfBackUtils.logSafe(motifCode));

        return ResponseEntity.ok(motifDto);
    }

    @PutMapping(path = "/activer/{langue}/{motifCode}")
    public ResponseEntity<MotifDTO> activerMotif(@PathVariable String langue, @PathVariable String motifCode) {

        LOGGER.info("Appel /activerMotif");

        MotifDTO motifDto = motifsService.activerMotif(motifCode, langue);

        LOGGER.info("======================= Fin /activerMotif. Le motif {} a été activé avec succès",
                AfBackUtils.logSafe(motifCode));

        return ResponseEntity.ok(motifDto);
    }

    private boolean isFrenchOnly() {
        // S'il n'y a qu'une langue on ne récupère que les motifs FR
        Map<String, String> langues = demarchesService.getLanguesDisponibles();
        return langues.size() == 1 && langues.containsKey("fr");
    }

}

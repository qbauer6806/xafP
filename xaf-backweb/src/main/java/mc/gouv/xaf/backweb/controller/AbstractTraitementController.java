package mc.gouv.xaf.backweb.controller;

import jakarta.el.PropertyNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.model.CommentaireInterneDTO;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.exception.FileUploadException;
import mc.gouv.xaf.back.exception.VScanException;
import mc.gouv.xaf.back.exception.enums.FileUploadErrorEnum;
import mc.gouv.xaf.back.service.AfApiService;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.backweb.formbean.XafTraitementFormBean;
import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import mc.gouv.xaf.backweb.ws.FileController;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeComplementsFileDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.FileCategoryDTO;
import mc.gouv.xaf.shared.dto.FileSubCategoryDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

public class AbstractTraitementController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTraitementController.class);

    private static final String ERROR_MESSAGES = "errorMessages";

    private static final String I18N_TRAITEMENT_CONCURRENT_DEPOTIC_ERROR_CODE_MESSAGE = "message.error.traitement.concurrent.depotIC";

    public static final String I18N_ENVOI_SUCCESS_CODE_MESSAGE = "message.success.envoi";

    // Messages sur l'upload d'un fichier
    private static final String I18N_UPLOAD_FICHIER_EXTENSION_NON_ACCEPTEE = "message.error.fileupload.extension";
    private static final String I18N_UPLOAD_FICHIER_ERREUR = "message.error.fileupload.error";
    private static final String I18N_UPLOAD_VSCAN_FICHIER_CORROMPU = "message.error.vscan.corrompu";
    private static final String I18N_UPLOAD_FICHIER_TAILLE_NON_ACCEPTEE = "message.error.fileupload.taille";

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private GouvBPM gouvBPM;

    @Autowired
    private FileController fileController;

    @Autowired
    private AfApiService afApiService;

    @Autowired
    private BackGouvPropertiesResolver backGouvPropertiesResolver;
    @Autowired
    private MotifsCache motifsCache;
    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    // Pour les informations liées à la demande
    private static final String I18N_SAUVEGARDE_SUCCESS_CODE_MESSAGE = "message.success.sauvegarde";

    private static final String REDIRECT = "redirect:";

    @Secured({ "ROLE_TRAITEMENT", "ROLE_VALIDATION", "ROLE_LECTURE" })
    @PostMapping(value = "/infosAdministration")
    @Transactional
    public ModelAndView infosAdministration(
            @ModelAttribute("traitementFormBean") XafTraitementFormBean xafTraitementFormBean,
            @RequestParam() Integer pkDemande, final RedirectAttributes redirectAttributes) {

        LOGGER.info("======================= Appel de la page /traitement/infosAdministration ({})", pkDemande);

        LOGGER.info("Appel à DEM pour stockage des observations...");
        DemandeDTO demUpd = new DemandeDTO();
        demUpd.setPkDemandes(pkDemande);
        demUpd.setObservations(xafTraitementFormBean.getObservations());
        demandesService.updateDemande(demUpd, true);

        xafTraitementFormBean.setObservations(null);

        LOGGER.info("======================= Fin /traitement/infosAdministration");

        return returnSuccessMessage(pkDemande, I18N_SAUVEGARDE_SUCCESS_CODE_MESSAGE, redirectAttributes);
    }

    @Secured({ "ROLE_TRAITEMENT", "ROLE_VALIDATION", "ROLE_LECTURE" })
    @ResponseBody
    @PostMapping(value = "/commentaires")
    @Transactional
    public CommentaireInterneDTO sauvegarderComm(
            @ModelAttribute("traitementFormBean") XafTraitementFormBean xafTraitementFormBean,
            @RequestParam() Integer pkDemande) {

        LOGGER.info("======================= Appel de la page /traitement/commentaires action=Ajouter ({})", pkDemande);

        String commString = xafTraitementFormBean.getCommentaireInterne();
        CommentaireInterneDTO commInterne = new CommentaireInterneDTO();
        if (!StringUtils.isBlank(commString)) {
            String safeComm = AfBackUtils.logSafe(commString);
            LOGGER.info("Commentaire : {}", safeComm);
            commInterne.setAgentId(AfBackUtils.getAuthenticatedAgentId());
            commInterne.setDate(new Date());
            commInterne.setCommentaire(commString);
            gouvBPM.putCommentaireInterne(pkDemande, commInterne);

        } else {
            throw new DemarcheException("Impossible d'insérer un commentaire vide");
        }

        LOGGER.info("======================= Fin /traitement/commentaires action=Ajouter");

        return commInterne;
    }

    @Secured("ROLE_TRAITEMENT")
    @PostMapping("/repondreDIC")
    @Transactional
    public ModelAndView repondreDIC(@RequestParam MultipartFile[] uploadingFiles, HttpServletResponse response,
                                    @RequestParam String commentaireReponse, @RequestParam Integer pkDemande, @RequestParam Integer icId,
                                    @RequestParam String activeTaskDefinitionKey, final RedirectAttributes redirectAttributes)
            throws IOException, TikaException, SAXException {

        String safeComm = commentaireReponse.replaceAll(SharedMessages.UNSAFE_CHARS, "_");
        LOGGER.info("Appel de la page /traitement/repondreDIC commentaireReponse = {}", safeComm);

        //Gestion pour voir si la tache courante est bien depotICTask
        GouvBPMTask activeTask = gouvBPM.getActiveTasksForDemande(pkDemande).getFirst();

        ModelAndView mav = checkActiveTask(pkDemande, activeTask, activeTaskDefinitionKey,
                I18N_TRAITEMENT_CONCURRENT_DEPOTIC_ERROR_CODE_MESSAGE, redirectAttributes);
        if (mav != null) {
            return mav;
        }

        LOGGER.info("Étape 1 : upload des fichiers dans FILE...");
        Map<String, String> fileNames;
        try {
            fileNames = fileController.saveFiles(pkDemande, uploadingFiles, response);
        } catch (FileUploadException e) {
            if (e.getError().equals(FileUploadErrorEnum.TAILLE_MAX_ERROR)) {
                // refs #29646 on gére les arguments du message d'erreur relatifs à la valeur
                // set dans les propriétés
                String maxFileSize = backGouvPropertiesResolver.getMaxFileSize();
                if (maxFileSize == null || maxFileSize.isEmpty()) {
                    throw new PropertyNotFoundException(
                            "La propriété obligatoire spring.servlet.multipart.max-file-size ne semble pas définie");
                }
                // Suppression de la partie "MB" pour récupérer uniquement le chiffre
                String numberPart = maxFileSize.replaceAll("[^0-9]", "");

                return returnErrorMessageWithArgs(pkDemande, I18N_UPLOAD_FICHIER_TAILLE_NON_ACCEPTEE,
                        redirectAttributes, new Object[]{numberPart});
            } else {
                return returnErrorMessage(pkDemande, I18N_UPLOAD_FICHIER_EXTENSION_NON_ACCEPTEE, redirectAttributes);
            }

        } catch (VScanException e) {
            return returnErrorMessage(pkDemande, I18N_UPLOAD_VSCAN_FICHIER_CORROMPU, redirectAttributes);
        } catch (Exception e) {
            return returnErrorMessage(pkDemande, I18N_UPLOAD_FICHIER_ERREUR, redirectAttributes);
        }

        LOGGER.info("Étape 2 : création de la réponse dans DEM...");
        DemandeComplementsReponseDTO reponse = new DemandeComplementsReponseDTO();
        reponse.setAgentId(AfBackUtils.getAuthenticatedAgentId());
        reponse.setTexte(commentaireReponse);
        List<DemandeComplementsFileDTO> complFiles = new ArrayList<>();
        fileNames.keySet().forEach(fileName -> {
            DemandeComplementsFileDTO file = new DemandeComplementsFileDTO();
            file.setName(fileName);
            file.setUrl(fileNames.get(fileName));
            complFiles.add(file);
        });
        reponse.setFichiers(complFiles.toArray(DemandeComplementsFileDTO[]::new));

        afApiService.repondreDemandeComplements(pkDemande, icId, reponse);

        LOGGER.info("======================= Fin /traitement/repondreDIC");

        mav = returnSuccessMessage(pkDemande, I18N_ENVOI_SUCCESS_CODE_MESSAGE, redirectAttributes);

        return mav;
    }

    protected ModelAndView returnSuccessMessage(Integer pkDemande, String messageCode,
            final RedirectAttributes redirectAttributes) {
        List<String> messages = new ArrayList<>();
        messages.add(messageSource.getMessage(messageCode, null, Locale.FRENCH));
        redirectAttributes.addFlashAttribute("successMessages", messages);
        return new ModelAndView(REDIRECT + pkDemande);
    }

    protected ModelAndView returnSuccessMessage(Integer pkDemande, String messageCode, String demandeTab,
            final RedirectAttributes redirectAttributes) {
        List<String> messages = new ArrayList<>();
        messages.add(messageSource.getMessage(messageCode, null, Locale.FRENCH));
        redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);
        String url = StringUtils.isBlank(demandeTab)
                ? REDIRECT + pkDemande
                : REDIRECT + pkDemande + "?demandeTab=" + demandeTab;
        return new ModelAndView(url);
    }

    protected ModelAndView returnErrorMessage(Integer pkDemande, String messageCode,
            final RedirectAttributes redirectAttributes) {
        List<String> messages = new ArrayList<>();
        messages.add(messageSource.getMessage(messageCode, null, Locale.FRENCH));
        redirectAttributes.addFlashAttribute(ERROR_MESSAGES, messages);
        return new ModelAndView(REDIRECT + pkDemande);
    }

    protected ModelAndView returnErrorMessage(Integer pkDemande, String messageCode, String demandeTab,
            final RedirectAttributes redirectAttributes) {
        List<String> messages = new ArrayList<>();
        messages.add(messageSource.getMessage(messageCode, null, Locale.FRENCH));
        redirectAttributes.addFlashAttribute(ERROR_MESSAGES, messages);
        String url = StringUtils.isBlank(demandeTab)
                ? REDIRECT + pkDemande
                : REDIRECT + pkDemande + "?demandeTab=" + demandeTab;
        return new ModelAndView(url);
    }

    protected ModelAndView returnErrorMessageWithArgs(Integer pkDemande, String messageCode,
            final RedirectAttributes redirectAttributes, Object[] args) {
        List<String> messages = new ArrayList<>();
        messages.add(messageSource.getMessage(messageCode, args, Locale.FRENCH));
        redirectAttributes.addFlashAttribute(ERROR_MESSAGES, messages);
        return new ModelAndView(REDIRECT + pkDemande);
    }

    /**
     * Vérifie que la soumission de la tache demandée est bien toujours la bonne dans le BPM
     */
    protected ModelAndView checkActiveTask(Integer pkDemande, GouvBPMTask activeTask, String activeTaskDefinitionKey,
            String messageCode, final RedirectAttributes redirectAttributes) {
        String safeActiveTask = AfBackUtils.logSafe(activeTaskDefinitionKey);
        LOGGER.info("Vérification {} = {}", safeActiveTask, activeTask.getTaskDefinitionKey());
        // Si l'active n'est plus la bonne souhaitée
        if (!StringUtils.equals(activeTaskDefinitionKey, activeTask.getTaskDefinitionKey())) {
            return returnErrorMessage(pkDemande, messageCode, redirectAttributes);

        }
        return null;
    }

    public ModelAndView form(String path, DemandeDTO demande) {
        ModelAndView mav = new ModelAndView(path);
        XafTraitementFormBean xafTraitementFormBean = new XafTraitementFormBean();
        xafTraitementFormBean.setObservations(demande.getObservations());
        MotifDTO motif = motifsCache.getMotif(demarchesDataProvider.getCodeMotifDemandeRectification(), "fr");
        if (motif != null) {
            xafTraitementFormBean.setTexteDemandeRectification(motif.getCommentairePrerempli());
        }
        mav.addObject("xafTraitementFormBean", xafTraitementFormBean);
        return mav;
    }

    protected int getFileCount(List<FileCategoryDTO> categories) {
        int fileCount = 0;
        for (FileCategoryDTO cat : categories) {
            if (CollectionUtils.isNotEmpty(cat.getFiles())) {
                fileCount += cat.getFiles().size();
            }
            if (CollectionUtils.isNotEmpty(cat.getSubCategories())) {
                fileCount += (int) cat.getSubCategories().stream().map(FileSubCategoryDTO::getFiles)
                        .filter(Objects::nonNull).flatMap(List::stream).filter(Objects::nonNull).count();
            }
        }
        return fileCount;
    }

}

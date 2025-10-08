package mc.gouv.xaf.backweb.controller;

import static mc.gouv.xaf.back.service.utils.AfBackUtils.hasRole;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.el.PropertyNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.exception.FileUploadException;
import mc.gouv.xaf.back.exception.VScanException;
import mc.gouv.xaf.back.exception.enums.FileUploadErrorEnum;
import mc.gouv.xaf.back.service.AfApiService;
import mc.gouv.xaf.back.service.UploadPieceJustificativeService;
import mc.gouv.xaf.back.service.data.DemandesCommentaireService;
import mc.gouv.xaf.back.service.data.DemandesComplementsFilesService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.backweb.formbean.XafTraitementFormBean;
import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import mc.gouv.xaf.backweb.ws.FileController;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeCommentaireDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsFileDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.FileCategoryDTO;
import mc.gouv.xaf.shared.dto.FileSubCategoryDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.UploadFileDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import mc.gouv.xaf.shared.formbean.TypedocFormBean;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
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
    private static final String I18N_TRAITEMENT_TYPECODE_NULL_ERROR_CODE_MESSAGE = "message.error.traitement.typecode.null";

    private static final String FICHIERS_TAB = "fichiers";

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
    private DemandesCommentaireService demandesCommentaireService;

    @Autowired
    private BackGouvPropertiesResolver backGouvPropertiesResolver;

    @Autowired
    private DemandesFilesService demandesFilesService;
    @Autowired
    private DemandesComplementsFilesService demandesComplementsFilesService;
    @Autowired
    private PropertiesService propertiesService;
    @Autowired
    private UploadPieceJustificativeService uploadPieceJustificativeService;

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
    public DemandeCommentaireDTO sauvegarderComm(
            @ModelAttribute("traitementFormBean") XafTraitementFormBean xafTraitementFormBean,
            @RequestParam() Integer pkDemande) {

        LOGGER.info("======================= Appel de la page /traitement/commentaires action=Ajouter ({})", pkDemande);

        String commString = xafTraitementFormBean.getCommentaireInterne();
        DemandeCommentaireDTO commInterne = new DemandeCommentaireDTO();
        if (!StringUtils.isBlank(commString)) {
            String safeComm = AfBackUtils.logSafe(commString);
            LOGGER.info("Commentaire : {}", safeComm);
            commInterne.setAgentId(AfBackUtils.getAuthenticatedAgentId());
            commInterne.setDate(new Date());
            commInterne.setCommentaire(commString);
            commInterne.setFkDemandes(pkDemande);
            demandesCommentaireService.putCommentaireInterne(commInterne);
        } else {
            throw new DemarcheException("Impossible d'insérer un commentaire vide");
        }

        String commentaireFormate = AfBackUtils.formatCommentaire(commInterne.getCommentaire());
        commInterne.setCommentaire(commentaireFormate);

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
                        redirectAttributes, new Object[] { numberPart });
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
        mav.addObject("xafTraitementFormBean", xafTraitementFormBean);
        boolean isAgentAssigned = StringUtils.isNotBlank(demande.getAgentAffecteId());
        mav.addObject("isDiscussionPanelDisplayed", isAgentAssigned);
        // upload des pièces justificatives
        mav.addObject("uploadPieceJustificativeActif", this.uploadPieceJustificativeActive(isAgentAssigned));
        mav.addObject("uploadPieceJustificativeVisible", this.uploadPieceJustificativeVisible());
        mav.addObject("extensionsWhitelist", this.getExtensionsWhitelist());
        mav.addObject("maxFileSize", this.getMaxTailleFichier());

        return mav;
    }

    private boolean uploadPieceJustificativeActive(boolean isAgentAssigned) {
        return hasRole("ROLE_TRAITEMENT") && isAgentAssigned;
    }

    private boolean uploadPieceJustificativeVisible() {
        PropertiesDTO property = propertiesService.getProperty("XAF_UPLOAD_PIECE_JUSTIFICATIVE_BO");
        return property != null && StringUtils.isNotBlank(property.getValue()) && Boolean.parseBoolean(
                property.getValue());
    }

    private String getExtensionsWhitelist() {
        String extensionsWhitelist = backGouvPropertiesResolver.getExtensionsWhitelist();
        return StringUtils.isNotBlank(extensionsWhitelist) ? extensionsWhitelist : "";
    }

    private String getMaxTailleFichier() {
        String maxFileSize = backGouvPropertiesResolver.getMaxFileSize();
        return StringUtils.isNotBlank(maxFileSize) ? maxFileSize : "";
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

    protected Boolean isLockedByUsager(Integer pkDemande, DemandeDTO demande) {
        if (demande == null) {
            demande = demandesService.getDemande(pkDemande);
        }
        LOGGER.info("Contenu = {}", demande.getContenu());
        /* la demande est lockée jusqu'à une date supérieure à la date en cours */
        Long now = Instant.now().toEpochMilli();

        if (demande.getModificationTimestamp() != null && demande.getModificationTimestamp().compareTo(now) > 0) {
            LOGGER.info(
                    "======================= isLockedByUsager demande {} : timestamp actuel: {} timestamp demande: {} diff: {}",
                    pkDemande, now, demande.getModificationTimestamp(), demande.getModificationTimestamp() - now);
            return true;
        }
        return false;
    }

    protected ModelAndView typageDocuments(TypedocFormBean typedocFormBean, Integer pkDemande,
            final RedirectAttributes redirectAttributes) {
        LOGGER.info("======================= Appel de la page /traitement/typageDocuments (DemandeID = {})", pkDemande);

        try {
            ObjectMapper mapper = new ObjectMapper();

            // Désérialisation des fichiers
            Map<String, String> files = mapper.readValue(typedocFormBean.getFiles(),
                    new TypeReference<HashMap<String, String>>() {

                    });

            // Une autre Map contenant les changements sur les checkboxes a été ajoutée
            Map<String, Boolean> filesCheckbox = mapper.readValue(typedocFormBean.getFilesCheckbox(),
                    new TypeReference<HashMap<String, Boolean>>() {

                    });

            // La méthode d'update prend en paramètre cette nouvelle Map
            boolean updateFiles = demandesFilesService.updateTypedocs(files, filesCheckbox);

            // Désérialisation des compléments
            Map<String, String> complements = mapper.readValue(typedocFormBean.getComplements(),
                    new TypeReference<HashMap<String, String>>() {

                    });

            // Même chose pour les checkboxes des compléments
            Map<String, Boolean> complementsCheckbox = mapper.readValue(typedocFormBean.getComplementsCheckbox(),
                    new TypeReference<HashMap<String, Boolean>>() {

                    });

            // Même chose pour l'update des compléments
            boolean updateComplements = demandesComplementsFilesService.updateTypedocs(complements,
                    complementsCheckbox);

            LOGGER.info("======================= Fin /traitement/typageDocuments");

            if (updateFiles && updateComplements) {
                return returnSuccessMessage(pkDemande, I18N_SAUVEGARDE_SUCCESS_CODE_MESSAGE, FICHIERS_TAB,
                        redirectAttributes);
            }
            return returnErrorMessage(pkDemande, I18N_TRAITEMENT_TYPECODE_NULL_ERROR_CODE_MESSAGE, FICHIERS_TAB,
                    redirectAttributes);

        } catch (Exception e) {
            LOGGER.error("Erreur lors du traitement de typageDocuments", e);
            return returnErrorMessage(pkDemande, I18N_TRAITEMENT_TYPECODE_NULL_ERROR_CODE_MESSAGE, FICHIERS_TAB,
                    redirectAttributes);
        }
    }

    /**
     * Permets d'uploader des pièces justificatives depuis le BO
     *
     * @param pkDemande
     *         l'identifiant de la demande
     * @param files
     *         les fichiers à ajouter
     * @param metadonnees
     *         mapping du nom de fichier, son type, visibilité de la pièce
     * @param response
     *         la réponse
     * @return le message
     */
    @Secured({ "ROLE_TRAITEMENT" })
    @PostMapping(value = "/upload/{pkDemande}")
    @Transactional
    public ResponseEntity<String> uploadPieceJustificative(@PathVariable Integer pkDemande,
            @RequestPart("files") MultipartFile[] files, @RequestPart("metadonnees") List<UploadFileDTO> metadonnees,
            HttpServletResponse response) {

        LOGGER.info("Apple à la méthode uploadPieceJustificative pour la demande {}", pkDemande);
        if (files == null || files.length == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Aucun fichier sélectionné");
        }
        if (CollectionUtils.isEmpty(metadonnees)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Aucun type de fichier sélectionné");
        }
        if (files.length != metadonnees.size()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Veuillez associer un type à chaque fichier sélectionné");
        }

        return uploadPieceJustificativeService.enregistrerPieceJustificative(pkDemande, files, metadonnees, response);
    }

    @Secured({ "ROLE_TRAITEMENT" })
    @PostMapping(value = "/suppression/{pkDemandeFile}")
    @Transactional
    public ResponseEntity<String> supprimerPieceJustificative(@PathVariable Integer pkDemandeFile) {
        LOGGER.info("Apple à la méthode supprimerPieceJustificative pour la pièce {}", pkDemandeFile);

        return uploadPieceJustificativeService.supprimerPieceJustificative(pkDemandeFile);
    }

    /**
     * Modifie la visibilité d'un fichier associé à une demande.
     *
     * @param pkDemandeFile
     *         l'identifiant du fichier dont la visibilité doit être modifiée
     * @param visibleUsager
     *         la nouvelle visibilité du fichier (true pour visible, false pour invisible) par l'usager
     * @return un objet ResponseEntity contenant un message indiquant le résultat de l'opération
     */
    @Secured({ "ROLE_TRAITEMENT" })
    @PostMapping(value = "/updateVisibilite/{pkDemandeFile}")
    @Transactional
    public ResponseEntity<String> changerVisibiliteFichier(@PathVariable Integer pkDemandeFile,
            @RequestParam Boolean visibleUsager) {
        LOGGER.info("Apple à la méthode changerVisibiliteFichier pour la pièce {} et param {}", pkDemandeFile,
                visibleUsager);

        return uploadPieceJustificativeService.changerVisibiliteFichier(pkDemandeFile, visibleUsager);
    }

}

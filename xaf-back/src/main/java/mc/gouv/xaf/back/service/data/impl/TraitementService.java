package mc.gouv.xaf.back.service.data.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static mc.gouv.xaf.back.service.utils.AfBackUtils.hasRole;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.el.PropertyNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.bpm.activiti.exception.TaskAlreadyClaimedException;
import mc.gouv.xaf.back.bpm.model.GouvBPMStatutAction;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.exception.FileUploadException;
import mc.gouv.xaf.back.exception.VScanException;
import mc.gouv.xaf.back.exception.enums.FileUploadErrorEnum;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.AfApiService;
import mc.gouv.xaf.back.service.DemandeFilesCategorizer;
import mc.gouv.xaf.back.service.DemandeRecapHTMLService;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.UploadPieceJustificativeService;
import mc.gouv.xaf.back.service.data.DemandesCommentaireService;
import mc.gouv.xaf.back.service.data.DemandesComplementsFilesService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.histo.DemandesHistoriqueService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.DemandeRecapDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.back.service.purge.PurgeDemandesService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.DemandesComplementsComparator;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.back.service.utils.UtilisateursUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeCommentaireDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsFileDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueAffichageDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;
import mc.gouv.xaf.shared.dto.FileCategoryDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.UploadFileDTO;
import mc.gouv.xaf.shared.dto.XafTraitementFormBean;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;
import mc.gouv.xaf.shared.exception.DemarcheException;
import mc.gouv.xaf.shared.formbean.TypedocFormBean;
import mc.gouv.xaf.shared.util.FileNameUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

@Component
@RequiredArgsConstructor
public class TraitementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TraitementService.class);
    public static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ERROR_MESSAGES = "errorMessages";

    private static final String I18N_TRAITEMENT_CONCURRENT_DEPOTIC_ERROR_CODE_MESSAGE = "message.error.traitement.concurrent.depotIC";
    private static final String I18N_ANNULATION_SUCCESS_CODE_MESSAGE = "message.success.annulation";
    private static final String I18N_TRAITEMENT_CONCURRENT_FINAL_ERROR_CODE_MESSAGE = "message.error.traitement.concurrent.final";
    public static final String I18N_ENVOI_SUCCESS_CODE_MESSAGE = "message.success.envoi";
    private static final String I18N_TRAITEMENT_CONCURRENT_PRIS_EN_CHARGE_ERROR_CODE_MESSAGE = "message.error.traitement.concurrent.priseencharge";

    // Messages sur l'upload d'un fichier
    private static final String I18N_UPLOAD_FICHIER_EXTENSION_NON_ACCEPTEE = "message.error.fileupload.extension";
    private static final String I18N_UPLOAD_FICHIER_ERREUR = "message.error.fileupload.error";
    private static final String I18N_UPLOAD_VSCAN_FICHIER_CORROMPU = "message.error.vscan.corrompu";
    private static final String I18N_UPLOAD_FICHIER_TAILLE_NON_ACCEPTEE = "message.error.fileupload.taille";
    private static final String I18N_TRAITEMENT_TYPECODE_NULL_ERROR_CODE_MESSAGE = "message.error.traitement.typecode.null";

    // Pour les informations liées à la demande
    private static final String I18N_SAUVEGARDE_SUCCESS_CODE_MESSAGE = "message.success.sauvegarde";
    private static final String LECTURE_ROLE = "ROLE_LECTURE";
    private static final String SAISIE_ROLE = "ROLE_SAISIE";

    public static final String REDIRECT = "redirect:";
    public static final String FICHIERS_TAB = "fichiers";
    public static final String DETAILS_TAB = "details";

    private final DemandesService demandesService;
    private final MessageSource messageSource;
    private final GouvBPM gouvBPM;
    private final FileService fileService;
    private final AfApiService afApiService;
    private final DemandesCommentaireService demandesCommentaireService;
    private final GouvPropertiesResolver gouvPropertiesResolver;
    private final DemarchesDataProvider demarchesDataProvider;
    private final DemandesFilesService demandesFilesService;
    private final DemandesComplementsFilesService demandesComplementsFilesService;
    private final PropertiesService propertiesService;
    private final UploadPieceJustificativeService uploadPieceJustificativeService;
    private final DemandesHistoriqueService demandesHistoriqueService;
    private final DemandesStatutsService demandesStatutsService;
    private final GUKafkaUtils guKafkaUtils;
    private final GUKafkaProducer guKafkaProducer;
    private final MotifsCache motifsCache;
    private final AfBackUtils afBackUtils;
    private final UtilisateursUtils utilisateursUtils;
    private final DemandeRecapHTMLService demandeRecapHTMLService;
    private final DemandeFilesCategorizer demandeFilesCategorizer;
    private final DemandesHelperService demandesHelperService;
    private final PurgeDemandesService purgeDemandesService;

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
            fileNames = saveFiles(pkDemande, uploadingFiles, response);
        } catch (FileUploadException e) {
            if (e.getError().equals(FileUploadErrorEnum.TAILLE_MAX_ERROR)) {
                // refs #29646 on gére les arguments du message d'erreur relatifs à la valeur
                // set dans les propriétés
                String maxFileSize = gouvPropertiesResolver.getMaxFileSize();
                if (maxFileSize == null || maxFileSize.isEmpty()) {
                    throw new PropertyNotFoundException(
                            "La propriété obligatoire spring.servlet.multipart.max-file-size ne semble pas définie");
                }
                // Suppression de la partie "MB" pour récupérer uniquement le chiffre
                String numberPart = maxFileSize.replaceAll("\\D", "");

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

    /**
     * Appelle FILE afin de sauvegarder différents fichiers contenus dans la request MultiPart Retourne une Map
     * correspondant aux fichiers (fileName, fileUrl)
     */
    private Map<String, String> saveFiles(Integer demandeId, MultipartFile[] files, HttpServletResponse response)
            throws IOException {
        LOGGER.info("Appel de DEM afin de sauvegarder différents fichiers contenus dans la request");
        DemandeDTO demande = demandesService.getDemande(demandeId);
        Map<String, String> fileNames = new HashMap<>();
        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();
            if (StringUtils.isNotBlank(originalFilename)) {
                String safeFileName = AfBackUtils.logSafe(originalFilename);
                LOGGER.info("Part à traiter : {}", safeFileName);
                String saveFile = fileService.saveFile(demande, gouvPropertiesResolver.getContainerId(), file,
                        response);

                // #41757 - On décode de l'url du fichier pour qu'il soit affiché en clair dans le FO
                fileNames.put(FileNameUtils.getSafeFileName(originalFilename), URLDecoder.decode(saveFile, UTF_8));
            }
        }
        return fileNames;
    }

    public ModelAndView returnSuccessMessage(Integer pkDemande, String messageCode,
            final RedirectAttributes redirectAttributes) {
        List<String> messages = new ArrayList<>();
        messages.add(messageSource.getMessage(messageCode, null, Locale.FRENCH));
        redirectAttributes.addFlashAttribute("successMessages", messages);
        return new ModelAndView(REDIRECT + pkDemande);
    }

    public ModelAndView returnSuccessMessage(Integer pkDemande, String messageCode, String demandeTab,
            final RedirectAttributes redirectAttributes) {
        List<String> messages = new ArrayList<>();
        messages.add(messageSource.getMessage(messageCode, null, Locale.FRENCH));
        redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);
        String url = StringUtils.isBlank(demandeTab)
                ? REDIRECT + pkDemande
                : REDIRECT + pkDemande + "?demandeTab=" + demandeTab;
        return new ModelAndView(url);
    }

    public ModelAndView returnErrorMessage(Integer pkDemande, String messageCode,
            final RedirectAttributes redirectAttributes) {
        List<String> messages = new ArrayList<>();
        messages.add(messageSource.getMessage(messageCode, null, Locale.FRENCH));
        redirectAttributes.addFlashAttribute(ERROR_MESSAGES, messages);
        return new ModelAndView(REDIRECT + pkDemande);
    }

    public ModelAndView returnErrorMessage(Integer pkDemande, String messageCode, String demandeTab,
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
    public ModelAndView checkActiveTask(Integer pkDemande, GouvBPMTask activeTask, String activeTaskDefinitionKey,
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
        // upload des pièces justificatives
        mav.addObject("uploadPieceJustificativeActif", this.uploadPieceJustificativeActive(isAgentAssigned));
        mav.addObject("uploadPieceJustificativeVisible", this.uploadPieceJustificativeVisible());
        mav.addObject("extensionsWhitelist", this.getExtensionsWhitelist());
        mav.addObject("maxFileSize", this.getMaxTailleFichier());

        mav.addObject("MotifsCache", motifsCache);
        mav.addObject("demandeur", demarchesDataProvider.getDemandeur(demande));
        mav.addObject("utilisateurAffecte", afBackUtils.getUtilisateurAffecte(demande));
        List<DemandeCommentaireDTO> commInternes = demandesCommentaireService.getCommentairesInternes(
                demande.getPkDemandes());
        mav.addObject("commInternes", commInternes);
        mav.addObject("isDocumentsValidesActif", AfBackUtils.isDocumentsValidesActif(demande));
        String utilisateurConnecte = utilisateursUtils.getUserNameFromID(AfBackUtils.getAuthenticatedAgentId());
        mav.addObject("utilisateurConnecte", utilisateurConnecte != null ? utilisateurConnecte : "");
        try {
            mav.addObject("demandeRecap", demandeRecapHTMLService.getHTMLDemandeContenuRecap(demande, false));
        } catch (IllegalArgumentException | SecurityException e) {
            throw new DemarcheException("Erreur lors de la génération du récap", e);
        }
        mav.addObject("typedocFormBean", new TypedocFormBean());
        // Définition des actions disponibles sur la page de traitement pour une demande en cours de traitement
        // Définir pour la page, les actions disponibles et leurs statuts cibles associés, si on est à une étape de choix
        List<GouvBPMStatutAction> actionsDisponibles = new ArrayList<>();
        // Extraire ces actions disponibles du diagramme BPM
        List<GouvBPMTask> activeTasks = gouvBPM.getActiveTasksForDemande(demande.getPkDemandes());
        if (activeTasks != null && !activeTasks.isEmpty()) {
            GouvBPMTask activeTask = activeTasks.getFirst();
            mav.addObject("activeTaskDefinitionKey", activeTask.getTaskDefinitionKey());
            actionsDisponibles = gouvBPM.getTaskStatutActions(activeTask);
        }
        // On enlève la demande de rectification si ce n'est pas guichet virtuel
        if (demande.getCanal() != DemandeCanalEnum.GUICHET_VIRTUEL) {
            actionsDisponibles.removeIf(a -> "EN_ATTENTE_RECTIFICATION".equals(a.getStatut()));
        }
        mav.addObject("actionsDisponibles", actionsDisponibles);

        // Si pas d'instance de process en cours pour la demande, cela veut dire que celle-ci est terminé
        boolean isTerminee = !gouvBPM.isProcessInstanceAlive(demande.getPkDemandes());
        mav.addObject("isTerminee", isTerminee);
        List<FileCategoryDTO> categories = demandeFilesCategorizer.getCategoriesAndFiles(demande);
        mav.addObject("categories", categories);
        mav.addObject("typedocNullNbr", FileUtils.getNbFileNonTypes(categories));
        Integer icId = this.getIcId(demande);
        if (icId != null) {
            mav.addObject("icId", icId);
        }
        // Tri des demandes d'informations complémentaires par date
        if (demande.getComplements() != null) {
            Arrays.sort(demande.getComplements(), new DemandesComplementsComparator());
        }
        mav.addObject(demande);

        mav.addObject("isGenerationZipActive", true);
        mav.addObject("isGenerationPdfActive", true);
        mav.addObject("isPanelTypeFichierOuvert", true);
        boolean isStatutSimplifieTermine = StatutSimplifieEnum.TERMINEE.equals(
                demarchesDataProvider.getStatutSimplifie(demande.getDernierStatut().getName()));
        mav.addObject("accesDesactive",
                isStatutSimplifieTermine && demandesService.isAccesDesactive(demande.getPkDemandes()));

        // Chargement de l'historique de la demande
        List<DemandeHistoriqueDTO> histosDem = demandesHistoriqueService.getHistorique(demande.getPkDemandes());
        List<DemandeHistoriqueAffichageDTO> historiqueAffichageDTOS = afBackUtils.histoDem2Ts(histosDem);
        mav.addObject("histos", historiqueAffichageDTOS);

        // L'agent peut éditer uniquement s'il n'a pas le rôle de secrétaire ni le rôle lecture seule.
        boolean isEditable = this.hasOtherRoleLectureAndSaisie();
        mav.addObject("isObservationPanelActive", isEditable);
        mav.addObject("isDiscussionPanelActive", !isStatutSimplifieTermine && isEditable);

        return mav;
    }

    /**
     * Détermine si l'utilisateur actuellement authentifié possède au moins un rôle différent des rôles spécifiés, à
     * savoir {@code LECTURE_ROLE} et {@code SAISIE_ROLE}.
     *
     * @return {@code true} si les autorités de l'utilisateur contiennent au moins un rôle ne correspondant pas à
     *         {@code LECTURE_ROLE} ou {@code SAISIE_ROLE} ; {@code false} sinon.
     */
    private boolean hasOtherRoleLectureAndSaisie() {
        var context = SecurityContextHolder.getContext();
        if (context == null || context.getAuthentication() == null || CollectionUtils.isEmpty(
                context.getAuthentication().getAuthorities())) {
            return false;
        }
        var authorities = context.getAuthentication().getAuthorities();
        // Retourne true si au moins un rôle de l'utilisateur n'est PAS LECTURE_ROLE ou SAISIE_ROLE
        return authorities.stream().map(GrantedAuthority::getAuthority)
                .anyMatch(role -> !StringUtils.equalsAny(role, LECTURE_ROLE, SAISIE_ROLE));
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
        String extensionsWhitelist = gouvPropertiesResolver.getExtensionsWhitelist();
        return StringUtils.isNotBlank(extensionsWhitelist) ? extensionsWhitelist : "";
    }

    private String getMaxTailleFichier() {
        String maxFileSize = gouvPropertiesResolver.getMaxFileSize();
        return StringUtils.isNotBlank(maxFileSize) ? maxFileSize : "";
    }

    private Integer getIcId(DemandeDTO demande) {
        Integer icId = null;
        if (StatutSimplifieEnum.EN_ATTENTE_USAGER.equals(
                demarchesDataProvider.getStatutSimplifie(demande.getDernierStatut().getName()))
                && demande.getComplements() != null) {
            // Si on est en attente d'informations complémentaires, donner à la page le icId de la demande d'IC la plus récente
            DemandeComplementsDTO latestCompl = null;
            for (DemandeComplementsDTO compl : demande.getComplements()) {
                if (latestCompl == null || latestCompl.getQuestion().getDate().before(compl.getQuestion().getDate())) {
                    latestCompl = compl;
                }
            }
            icId = latestCompl != null ? latestCompl.getPkDemandeComplements() : null;
        }
        return icId;
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

    public ModelAndView typageDocuments(TypedocFormBean typedocFormBean, Integer pkDemande,
            final RedirectAttributes redirectAttributes) {
        LOGGER.info("======================= Appel de la page /traitement/typageDocuments (DemandeID = {})", pkDemande);

        try {
            // Désérialisation des fichiers
            Map<String, String> files = MAPPER.readValue(typedocFormBean.getFiles(),
                    new TypeReference<HashMap<String, String>>() {

                    });

            // Une autre Map contenant les changements sur les checkboxes a été ajoutée
            Map<String, Boolean> filesCheckbox = MAPPER.readValue(typedocFormBean.getFilesCheckbox(),
                    new TypeReference<HashMap<String, Boolean>>() {

                    });

            // La méthode d'update prend en paramètre cette nouvelle Map
            boolean updateFiles = demandesFilesService.updateTypedocs(files, filesCheckbox);

            // Désérialisation des compléments
            Map<String, String> complements = MAPPER.readValue(typedocFormBean.getComplements(),
                    new TypeReference<HashMap<String, String>>() {

                    });

            // Même chose pour les checkboxes des compléments
            Map<String, Boolean> complementsCheckbox = MAPPER.readValue(typedocFormBean.getComplementsCheckbox(),
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

    public ModelAndView dupliquer(@RequestParam() Integer pkDemande) throws JsonProcessingException {

        DemandeDTO demandeDupliquee = this.dupliquerDemande(pkDemande);
        ModelAndView mav = new ModelAndView(REDIRECT + demandeDupliquee.getPkDemandes());

        LOGGER.info("======================= Fin /traitement/dupliquer");

        return mav;
    }

    public DemandeDTO dupliquerDemande(Integer pkDemande) {
        LOGGER.info("======================= Appel de la page /traitement/dupliquer ({})", pkDemande);

        LOGGER.info("Appel à DEM pour dupliquer la demande... {}", pkDemande);
        DemandeDTO demandeDupliquee = null;

        try {
            demandeDupliquee = demandesService.cloneDemande(pkDemande);

            LOGGER.info("Nouvelle demande : {}", demandeDupliquee.getPkDemandes());

            LOGGER.info("Appel à DEM pour créer un nouveau statut \"En attente\"");
            DemandeBO demandeBO = demandesHelperService.getCheckDemarcheDemandeBO(demandeDupliquee.getPkDemandes(),
                    false);
            demandesStatutsService.updateStatut(demandeBO, demarchesDataProvider.getPremierStatutCreationDemande(),
                    demandeDupliquee.getAgentAffecteId(), demandeDupliquee.getUsagerId(), "DUPLICATION",
                    "Demande dupliquée", "DUPLICATION");

            LOGGER.info("Création d'une instance de process dans le BPM pour cette demande ({})...",
                    demandeDupliquee.getPkDemandes());
            GouvBPMUser user = new GouvBPMUser();
            user.setId(demandeDupliquee.getUsagerId().toString());

            String canal = demandeDupliquee.getCanal().name();

            // Définition des process variables
            Map<String, Object> variables = new HashMap<>();

            variables.put(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_CANAL.name(), canal);
            variables.put(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_LANGUE.name(),
                    StringUtils.lowerCase(demandeDupliquee.getLangue()));
            variables.put(GouvBPMProcessVariableTypeEnum.MC_USAGERID.name(), demandeDupliquee.getUsagerId());
            variables.put(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_IDENTIFIANT.name(),
                    demandeDupliquee.getIdentifiant());

            gouvBPM.startProcessInstanceByMessage("duplicationMessage", user, demandeDupliquee.getPkDemandes(),
                    variables);

        } catch (Exception e) {
            if (demandeDupliquee != null && demandeDupliquee.getPkDemandes() != null) {
                LOGGER.error("Suppression de la demande dans DEM id:{} identifiant:{}",
                        demandeDupliquee.getPkDemandes(), demandeDupliquee.getIdentifiant());
                purgeDemandesService.deleteDemande(demandeDupliquee.getPkDemandes());
            }

            // Renvoi d'une exception pour que l'utilisateur sache qu'il y a eu une erreur
            throw new DemarcheException("Erreur lors de la création d'une demande", e);
        }

        // Ajout d'une ligne à l'historique
        DemandeDTO ancienneDemande = demandesService.getDemande(pkDemande);
        DemandeHistoriqueDTO histoNouvelleDemande = demandesHistoriqueService.historiqueDuplicationNouvelleDemande(
                ancienneDemande);
        DemandeHistoriqueDTO histoAncienneDemande = demandesHistoriqueService.historiqueDuplicationAncienneDemande(
                demandeDupliquee, ancienneDemande.getDernierStatut().getName());
        demandesHistoriqueService.saveHisto(demandeDupliquee.getPkDemandes(), histoNouvelleDemande);
        demandesHistoriqueService.saveHisto(pkDemande, histoAncienneDemande);
        LOGGER.info("Envoi du message au Guichet Unique via Kafka (création demande)...");
        List<DemandeRecapDTO> demandeRecaps = guKafkaUtils.getDemandeRecapsFromUsagerId(demandeDupliquee.getUsagerId());
        RecapDemandesDTO recapDemandes = guKafkaUtils.getRecapDemandes(demandeRecaps);
        guKafkaProducer.sendCreationDemandeMessage(demandeDupliquee.getUsagerId(), demandeDupliquee.getPkDemandes(),
                demandeDupliquee.getIdentifiant(), demandeDupliquee.getDateCreation(), recapDemandes);

        return demandeDupliquee;
    }

    public ModelAndView annuler(@RequestParam() Integer pkDemande, @RequestParam() String commentaire,
            @RequestParam() String codeMotif, final RedirectAttributes redirectAttributes) {

        LOGGER.info("======================= Appel de la page /traitement/Annuler ({})", pkDemande);

        GouvBPMUser agent = new GouvBPMUser();
        agent.setId(AfBackUtils.getAuthenticatedAgentId());

        Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(pkDemande);
        variables.put(GouvBPMProcessVariableTypeEnum.MC_ANNULATION_ORIGINATOR_USAGER.name(), null);
        gouvBPM.setProcessBusinessVariables(pkDemande, variables);

        gouvBPM.annulerDemande(pkDemande, agent, null, codeMotif, commentaire,
                demarchesDataProvider.getStatutAnnulee());

        DemandeHistoriqueDTO histo = demandesHistoriqueService.statusChangeAgent(
                demarchesDataProvider.getStatutAnnulee());
        saveHistorique(pkDemande, histo);

        LOGGER.info("======================= Fin /traitement/prendreEnCharge");

        return returnSuccessMessage(pkDemande, I18N_ANNULATION_SUCCESS_CODE_MESSAGE, redirectAttributes);
    }

    private void saveHistorique(Integer pkDemande, DemandeHistoriqueDTO histo) {
        LOGGER.info("Appel à DEM pour historique...");
        try {
            demandesHistoriqueService.saveHisto(pkDemande, histo);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la création de l'historique {}", histo, e);
        }
    }

    public ModelAndView reprendreEnCharge(@RequestParam() Integer pkDemande) {

        LOGGER.info("======================= Appel de la page /traitement/reprendreEnCharge ({})", pkDemande);

        String agentId = AfBackUtils.getAuthenticatedAgentId();
        // On change l'assignee
        gouvBPM.setAssignee(pkDemande, agentId);

        DemandeDTO demande = demandesService.changerAffectationDemande(pkDemande, agentId);

        // Ajout d'une ligne à l'historique, le statut cible est le même que dernierStatut dans une reprise en charge
        String dernierStatut = demande.getDernierStatut().getName();
        DemandeHistoriqueDTO histo = demandesHistoriqueService.statusChangeAgent(dernierStatut, agentId, dernierStatut);
        saveHistorique(pkDemande, histo);

        ModelAndView mav = new ModelAndView(REDIRECT + pkDemande);

        LOGGER.info("======================= Fin /traitement/reprendreEnCharge");

        return mav;
    }

    public ModelAndView prendreEnCharge(@RequestParam() Integer pkDemande,
            @RequestParam() String activeTaskDefinitionKey, final RedirectAttributes redirectAttributes)
            throws TaskAlreadyClaimedException, TikaException, IOException, SAXException {

        LOGGER.info("======================= Appel de la page /traitement/prendreEnCharge ({})", pkDemande);

        List<GouvBPMTask> activeTasks = gouvBPM.getActiveTasksForDemande(pkDemande);
        if (activeTasks == null || activeTasks.isEmpty()) {
            return returnErrorMessage(pkDemande, I18N_TRAITEMENT_CONCURRENT_FINAL_ERROR_CODE_MESSAGE,
                    redirectAttributes);
        }
        GouvBPMTask activeTask = activeTasks.getFirst();

        ModelAndView mav = checkActiveTask(pkDemande, activeTask, activeTaskDefinitionKey,
                I18N_TRAITEMENT_CONCURRENT_PRIS_EN_CHARGE_ERROR_CODE_MESSAGE, redirectAttributes);
        if (mav != null) {
            return mav;
        }
        LOGGER.info("claimTask() puis submitTaskFormData()...");
        GouvBPMUser user = new GouvBPMUser();
        String userId = AfBackUtils.getAuthenticatedAgentId();
        user.setId(userId);
        gouvBPM.setAssignee(pkDemande, userId);

        gouvBPM.claimTask(activeTask, user);
        gouvBPM.submitTaskFormData(activeTask, null, pkDemande);

        // Mettre à jour l'agent affecté dans DEM
        LOGGER.info("Appel de DEM pour définir l'agent affectué...");
        DemandeDTO demande = demandesService.changerAffectationDemande(pkDemande, userId);

        // Ajout d'une ligne à l'historique
        DemandeHistoriqueDTO histo = demandesHistoriqueService.statusChangeAgent(demande.getDernierStatut().getName());
        saveHistorique(pkDemande, histo);

        mav = new ModelAndView(REDIRECT + pkDemande);

        LOGGER.info("======================= Fin /traitement/prendreEnCharge");

        return mav;
    }

    /**
     * Claim la tache et vérifie qu'il y a pas d'erreur de concurrence Si c'est le cas , retourne le modelAndView
     *
     * @param task
     * @param user
     * @return
     */
    public ModelAndView claimTask(Integer pkDemande, GouvBPMTask task, GouvBPMUser user,
            final RedirectAttributes redirectAttributes) {
        try {
            gouvBPM.claimTask(task, user);
        } catch (TaskAlreadyClaimedException e1) {
            LOGGER.error("La tâche {} ({}) est déjà attribuée !", task.getId(), task.getTaskDefinitionKey());
            return returnErrorMessage(pkDemande, I18N_TRAITEMENT_CONCURRENT_PRIS_EN_CHARGE_ERROR_CODE_MESSAGE,
                    redirectAttributes);
        }
        return null;
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

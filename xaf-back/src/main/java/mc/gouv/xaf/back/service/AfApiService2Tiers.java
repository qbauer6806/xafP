package mc.gouv.xaf.back.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.BrouillonsService;
import mc.gouv.xaf.back.service.data.DemandesConfigService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PeriodesOuvertureService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.data.impl.MotifsServiceImpl;
import mc.gouv.xaf.back.service.itg.file.service.dto.FileResponseDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.UsagerDemandesRecapDTO;
import mc.gouv.xaf.back.service.itg.nomen.PaysCache;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.DemandeInputDTO;
import mc.gouv.xaf.shared.dto.DonneesMConnectDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.dto.Page;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import mc.gouv.xaf.shared.dto.PaysDTO;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;
import mc.gouv.xaf.shared.exception.DemarcheException;

/**
 * Services proposés par le module API 2 Tiers des TS (donc appelé par le système tiers, via le proxy 2 tiers)
 * 
 * @author qdeme
 * 
 */
@ConditionalOnExpression(value = "'${mc.gouv.${application.name}.frontserver.2tiers.activation}' == 'true'")
@Component
public class AfApiService2Tiers implements AfApi, AfApi2Tiers {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfApiService2Tiers.class);

    private static final String FILE_PATH = "/api2tiers/v1/file/";
    private static final String LOG_CHEMIN = "Chemin du fichier récupéré dans la requête : {}";

    @Autowired
    private MotifsServiceImpl motifsService;

    @Autowired
    private PeriodesOuvertureService periodesOuvertureService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private UsagersCache usagersCache;

    @Autowired
    private GUKafkaProducer guKafkaProducer;
    
    @Autowired
    private DemandesConfigService demandesConfigService;
    
    @Autowired
    private PaysCache paysCache;
    
    @Autowired
    private AccessService accessService;

    @Autowired
    private AfBackUtils afBackUtils;
    
    @Autowired
    private BrouillonsService brouillonsService;
    
    @Autowired
    private DemandesService demandesService;

    public void annulerDemande(Integer demandeId, Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.annulerDemande({}, {})", demandeId, usagerId);
        afBackUtils.getAfApiClient2Tiers().annulerDemande(demandeId, usagerId);
    }

    public DemandeComplementsDTO repondreDemandeComplements(Integer demandeId, Integer icId,
            DemandeComplementsReponseDTO reponse) {
        LOGGER.info("AfApiService2Tiers.repondreDemandeComplements({}, {}, {})", demandeId, icId, reponse);
        return afBackUtils.getAfApiClient2Tiers().repondreDemandeComplements(demandeId, icId, reponse);
    }

    public DemandeDTO getDemande(Integer usagerId, Integer demandeId) {
        LOGGER.info("AfApiService2Tiers.getDemande({}, {})", usagerId, demandeId);
        DemandeDTO demande = afBackUtils.getAfApiClient2Tiers().getDemande(usagerId, demandeId);

        return processDemandeFromSystemeTiers(demande);
    }

    public Page<DemandeDTO> getDemandesPageable(Integer usagerId, PageParamDTO paramDTO) {
        LOGGER.info("AfApiService2Tiers.getDemandesPageable({})", usagerId);
        Page<DemandeDTO> page = afBackUtils.getAfApiClient2Tiers().getDemandesPageable(usagerId, paramDTO);
        
        for (DemandeDTO demande : page.getContent()) {
            processDemandeFromSystemeTiers(demande);
        }
        return page;
    }

    public List<DemandeComplementsDTO> getDemandeComplements(Integer demandeId) {
        LOGGER.info("AfApiService2Tiers.getDemandeComplements({})", demandeId);
        return afBackUtils.getAfApiClient2Tiers().getDemandesComplements(demandeId);
    }

    public DemandeComplementsDTO getDemandeComplements(Integer demandeId, Integer icId) {
        LOGGER.info("AfApiService2Tiers.getDemandeComplements({}, {})", demandeId, icId);
        return afBackUtils.getAfApiClient2Tiers().getDemandeComplements(demandeId, icId);
    }

    public DemandeDTO associerDemandeCourrier(String identifiantDemande, String stringToCheck, Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.associerDemandeCourrier({}, {}, {})", identifiantDemande, stringToCheck,
                usagerId);
        return afBackUtils.getAfApiClient2Tiers().associerDemandeCourrier(identifiantDemande, stringToCheck, usagerId);
    }

    public void desinscriptionUsager(Integer usagerId, String langue, boolean fromGU) {
        LOGGER.info("AfApiService2Tiers.desinscriptionUsager({}, {})", usagerId, langue);
        
        LOGGER.info("Suppression des brouillons...");
        brouillonsService.deleteBrouillons(usagerId);

        LOGGER.info("Suppression de l'accès...");
        accessService.deleteAccess(usagerId);
        
        LOGGER.info("Envoi d'un message sur Kafka...");
        guKafkaProducer.sendDesinscriptionUsagerTSMessage(usagerId);
        
        LOGGER.info("Notification du système tiers...");
        afBackUtils.getAfApiClient2Tiers().desinscriptionUsager(usagerId, langue);
    }

    public AccessDTO createOrUpdateAccess(Integer usagerId, AccessInputDTO dto) {
        LOGGER.info("AfApiService2Tiers.createOrUpdateAccess({}, +dto)", usagerId);

        AccessDTO accessDto = new AccessDTO();
        accessDto.setUsagerId(usagerId);
        accessDto.setContenu(dto.getContenu());
        AccessDTO access = accessService.saveOrUpdateAccess(usagerId, accessDto);
        
        // Notifier MonGuichet de la création de l'accès
        guKafkaProducer.sendCreationAccesTSMessage(usagerId);
        return access;
    }

    public AccessDTO getAccess(Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.getAccess({})", usagerId);
        return accessService.getAccessActive(usagerId);
    }

    public UsagerCourrierDTO getUsagerCourrier(Integer usagerCourrierId) {
        LOGGER.info("AfApiService2Tiers.getUsagerCourrier({})", usagerCourrierId);
        return afBackUtils.getAfApiClient2Tiers().getUsagerCourrier(usagerCourrierId);
    }

    public List<MotifDTO> getMotifs() {
        LOGGER.info("AfApiService2Tiers.getMotifs()");
        // Décision prise de ne plus gérer les motifs nous même mais d'appeler le système tiers pour les récupérer
        return afBackUtils.getAfApiClient2Tiers().getMotifs();
    }

    public DemandeDTO creerDemande(DemandeInputDTO demande, Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.creerDemande({}, {})", demande, usagerId);
        
        // Injecter la fk vers la config actuelle, que le système tiers devra stocker
        demande.setBuildId(demandesConfigService.getLastBuildId());
        
        DemandeDTO demandeCreee = afBackUtils.getAfApiClient2Tiers().creerDemande(demande, usagerId);
        
        // Suppression du brouillon éventuel
        if (demande.getBrouillonId() != null) {
            LOGGER.info("Suppression du brouillon associé à la demande (brouillonId={})", demande.getBrouillonId());
            brouillonsService.deleteBrouillon(demande.getBrouillonId(), usagerId, true);
        }
        
        return demandeCreee;
    }

    public List<PeriodeOuvertureDTO> getPeriodesOuverture() {
        LOGGER.info("AfApiService2Tiers.getPeriodesOuverture()");
        return periodesOuvertureService.getPeriodesOuverture();
    }

    public ResponseEntity getCustomRequest(HttpServletRequest request, Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.getCustom()");
        return null;
    }

    public ResponseEntity postCustomRequest(HttpServletRequest request, Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.postCustom()");
        return null;
    }

    public ResponseEntity putCustomRequest(HttpServletRequest request, Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.putCustom()");
        return null;
    }

    public ResponseEntity deleteCustomRequest(HttpServletRequest request, Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.deleteCustom()");
        return null;
    }

    public List<PropertiesDTO> getFrontProperties() {
        LOGGER.info("AfApiService2Tiers.getFrontProperties()");
        return propertiesService.getProperties();
    }

    public BrouillonDTO creerBrouillon(BrouillonDTO brouillon, Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.creerBrouillon({}, {})", brouillon, usagerId);
        BrouillonDTO brouillonDto = null;
        try {

            brouillonDto = new BrouillonDTO();
            brouillonDto.setUsagerId(usagerId);
            brouillonDto.setPkBrouillons(null);
            brouillonDto.setContenu(brouillon.getContenu());
            brouillonDto.setFichiers(brouillon.getFichiers());
            brouillonDto.setRecapType(brouillon.getRecapType());
            brouillonDto.setMeta(brouillon.getMeta());
            brouillonDto.setContenuInitial(brouillon.getContenuInitial());

            brouillonDto = brouillonsService.saveOrUpdateBrouillon(brouillonDto, usagerId, false);

        } catch (Exception e) {
            LOGGER.error("Erreur lors de la création d'un brouillon {}", brouillonDto);
            // Renvoi d'une exception pour que l'utilisateur sache qu'il y a eu une erreur
            throw new DemarcheException("Erreur lors de la création d'un brouillon", e);
        }
        return brouillonDto;
    }

    public BrouillonDTO updateBrouillon(BrouillonDTO brouillon, Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.updateBrouillon({}, {})", brouillon, usagerId);
        BrouillonDTO brouillonDto;
        try {

            brouillonDto = brouillonsService.saveOrUpdateBrouillon(brouillon, usagerId, false);

        } catch (Exception e) {
            // Renvoi d'une exception pour que l'utilisateur sache qu'il y a eu une erreur
            throw new DemarcheException("Erreur lors de la mise à jour d'un brouillon", e);
        }
        return brouillonDto;
    }

    public Page<BrouillonDTO> getBrouillonsPageable(Integer usagerId, PageParamDTO paramDTO) {
        LOGGER.info("AfApiService2Tiers.getBrouillonsPageable({})", usagerId);
        return brouillonsService.getBrouillonsPageable(usagerId, paramDTO);
    }

    public BrouillonDTO getBrouillon(Integer pkBrouillons, Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.getBrouillon({}, {})", pkBrouillons, usagerId);
        return brouillonsService.getBrouillon(pkBrouillons, usagerId);
    }

    public void deleteBrouillon(Integer pkBrouillons, Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.deleteBrouillon({}, {})", pkBrouillons, usagerId);
        brouillonsService.deleteBrouillon(pkBrouillons, usagerId, false);
    }
    
    public byte[] getDemandeRecap(Integer usagerId, Integer demandeId, DonneesMConnectDTO donneesMConnectDTO) {
        LOGGER.info("AfApiService2Tiers.getDemandeRecap({}, {}, {})", usagerId, demandeId, donneesMConnectDTO);
        return afBackUtils.getAfApiClient2Tiers().getDemandeRecap(demandeId, usagerId, donneesMConnectDTO);
    }
    
    @Transactional
    public DemandeDTO updateDemande(Integer demandeId, DemandeInputDTO demande, Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.updateDemande({}, {})", demandeId, usagerId);
        return afBackUtils.getAfApiClient2Tiers().updateDemande(demandeId, demande, usagerId);
    }
    
    @Override
    public DemandeDTO lockDemande(Integer demandeId, Integer usagerId, Long timestamp) throws JsonProcessingException {
        return afBackUtils.getAfApiClient2Tiers().lockDemande(demandeId, usagerId, timestamp);
    }

    @Override
    public DemandeDTO unlockDemande(Integer demandeId, Integer usagerId) throws JsonProcessingException {
        return afBackUtils.getAfApiClient2Tiers().unlockDemande(demandeId, usagerId);
    }

    // ================================= 2EME PARTIE DE L'API =================================

    public PeriodeOuvertureDTO createPeriodeOuverture(PeriodeOuvertureDTO periodeOuverture) {
        return periodesOuvertureService.saveOrUpdatePeriodeOuverture(periodeOuverture);
    }

    public PeriodeOuvertureDTO updatePeriodeOuverture(PeriodeOuvertureDTO periodeOuverture) {
        return periodesOuvertureService.saveOrUpdatePeriodeOuverture(periodeOuverture);
    }

    public void deletePeriodeOuverture(Integer pkPeriodeOuverture) {
        periodesOuvertureService.deletePeriodeOuverture(pkPeriodeOuverture);
    }

    public FileResponseDTO saveFile(Integer usagerId, MultipartFile data, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // Seule manière avec Spring de pouvoir inclure des "/" dans le dernier
        // paramètre d'une URL (et en mettant /** dans l'URL)
        // (+ utilisation de la classe WebMvcConfig afin d'éviter que les
        // extensions ne soient traitées par Spring)
        String file = request.getServletPath();
        file = file.replace(FILE_PATH, "");
        file = file.split("/", 2)[1];
        LOGGER.info(LOG_CHEMIN, file);

        // Normalisation du nom de fichier... Exemple de quelqu'un qui uploaderait un "é" avec 65CC81 au lieu de C3A9
        file = Normalizer.normalize(file, Form.NFC);

        String account = gouvPropertiesResolver.getDemarcheId();

        LOGGER.info("====================== saveFile({},{}/{}/{})", usagerId, account, FileUtils.DEFAULT_CONTAINER, file);
        
        // Ajout de l'accès et de l'UUID en préfixe, avant soumission à FILE
        AccessBO access = accessService.getAccessBO(usagerId, true);
        if (access == null) {
            throw new DemarchesServiceException("Impossible de récupérer l'accès associé à l'usagerId fourni (" + usagerId + ")", HttpStatus.NOT_FOUND);
        }
        file = access.getPkAccess() + "/" + UUID.randomUUID() + "/" + file;

        Map<String, String> meta = extractMeta(request);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectMapper objectMapper = new ObjectMapper();
        FileResponseDTO fileResponseDTO = null;
        try {
            afBackUtils.getFileClient()
                    .saveFile(account, FileUtils.DEFAULT_CONTAINER, data.getInputStream(), file, data.getContentType(), meta, os);
            fileResponseDTO = objectMapper.readValue(os.toByteArray(), FileResponseDTO.class);
            
            // Override du champ "message" du ResponseDTO afin d'y indiquer le filename effectif correspondant à la ressource stockée.
            // Car, en mode 2/3, c'est l'API GenTS qui génère l'UUID (voir au dessus), et non le système tiers.
            // Il faut donc trouver un moyen d'indiquer au système tiers l'URL du fichier stocké.
            fileResponseDTO.setMessage(file);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'appel à FILE pour la sauvegarde du fichier", e);
        }
        
        return fileResponseDTO;
    }

    public ResponseEntity<InputStreamResource> getFile(HttpServletRequest request,
            HttpServletResponse response) {
        String file = request.getServletPath();
        file = file.replace(FILE_PATH, "");
        //file = file.split("/", 2)[1];
        LOGGER.info(LOG_CHEMIN, file);

        // Normalisation du nom de fichier... Exemple de quelqu'un qui uploaderait un "é" avec 65CC81 au lieu de C3A9
        file = Normalizer.normalize(file, Form.NFC);

        String account = gouvPropertiesResolver.getDemarcheId();

        LOGGER.info("====================== getFile({}/{}/{})", account, FileUtils.DEFAULT_CONTAINER, file);
        try {
            afBackUtils.getFileClient().getFile(account, FileUtils.DEFAULT_CONTAINER, file, response);
        } catch (IOException e) {
            LOGGER.error("Erreur lors de l'appel à FILE pour la récupération du fichier", e);
        }

        // Réponse déjà mise dans "response" par fileClient.getFile()
        return null;
    }

    public ResponseEntity deleteFile(HttpServletRequest request) {
        String file = request.getServletPath();
        file = file.replace(FILE_PATH, "");
        //file = file.split("/", 2)[1];
        LOGGER.info(LOG_CHEMIN, file);

        // Normalisation du nom de fichier... Exemple de quelqu'un qui uploaderait un "é" avec 65CC81 au lieu de C3A9
        file = Normalizer.normalize(file, Form.NFC);

        String account = gouvPropertiesResolver.getDemarcheId();

        LOGGER.info("====================== deleteFile({}/{}/{})", account, FileUtils.DEFAULT_CONTAINER, file);

        try {
            afBackUtils.getFileClient().deleteFile(account, FileUtils.DEFAULT_CONTAINER, file);
        } catch (Exception e) {
            LOGGER.info("Erreur lors de l'appel à FILE pour la suppression du fichier", e);
        }

        return ResponseEntity.ok().body(null);
    }

    public ResponseEntity notifyCreationDemande(Integer usagerId, Integer demandeId, String identifiantDemande,
            Date dateCreation, RecapDemandesDTO recapDemandes) {
        LOGGER.info("AfApiService2Tiers.notifyCreationDemande({},{},{},{},{})", usagerId, demandeId, identifiantDemande,
                dateCreation, recapDemandes);

        guKafkaProducer.sendCreationDemandeMessage(usagerId, demandeId, identifiantDemande, dateCreation,
                recapDemandes);

        return ResponseEntity.ok().body(null);
    }

    public ResponseEntity notifyChangementStatutDemande(Integer usagerId, Integer demandeId, String identifiantDemande,
            StatutSimplifieEnum statutSimplifie, Date dateStatutSimplifie, RecapDemandesDTO recapDemandes) {
        LOGGER.info("AfApiService2Tiers.notifyChangementStatutDemande({},{},{},{},{},{})", usagerId, demandeId,
                identifiantDemande, statutSimplifie, dateStatutSimplifie, recapDemandes);

        guKafkaProducer.sendChangementStatutDemandeMessage(usagerId, demandeId, identifiantDemande, statutSimplifie,
                dateStatutSimplifie, recapDemandes);

        return ResponseEntity.ok().body(null);
    }

    public ResponseEntity notifySuppressionDemande(Integer usagerId, Integer demandeId, String identifiantDemande,
            Date dateSuppression, RecapDemandesDTO recapDemandes) {
        LOGGER.info("AfApiService2Tiers.notifySuppressionDemande({},{},{},{},{})", usagerId, demandeId,
                identifiantDemande, dateSuppression, recapDemandes);

        guKafkaProducer.sendSuppressionDemandeMessage(usagerId, demandeId, identifiantDemande, dateSuppression,
                recapDemandes);

        return ResponseEntity.ok().body(null);
    }

    public ResponseEntity synchronizeDemandesRecaps(List<UsagerDemandesRecapDTO> usagerDemandesRecap) {
        LOGGER.info("AfApiService2Tiers.synchronizeDemandesRecaps({})", usagerDemandesRecap);

        guKafkaProducer.sendSynchronisationDemandesMessage(usagerDemandesRecap);

        return ResponseEntity.ok().body(null);
    }

    private Map<String, String> extractMeta(HttpServletRequest request) {
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            if (header.toLowerCase().startsWith(FileUtils.MC_METADATA_PREFIX.toLowerCase())) {
                String key = header.substring(FileUtils.MC_METADATA_PREFIX.length(), header.length());
                String value = request.getHeader(header);
                headerMap.put(key, value);
            }
        }
        return headerMap;
    }
    
    public JsonNode getDonneesExternes(Integer usagerId, Map<String, String[]> params)
            throws Exception {
        return null;
    }
    
    // TODO A SUPPRIMER car devenu inutile dans XAF12 (ne sera plus appelé)
    public void deleteFile(String string) {
        // TODO Auto-generated method stub
    }
    
    @Transactional
    public JsonNode creerConfig(JsonNode config) {
        return demandesConfigService.saveConfig(config);
    }
    
    public List<PaysDTO> getPays() {
        return new ArrayList<>(paysCache.getValues());
    }
    
    /**
     * 
     * Méthode servant à compléter le DemandeDTO remis par le système tiers avant restitution au FO
     * Car il a quelques ajustements à faire dans le cadre de la solution 2/3, dans laquelle l'API se place en intermédiaire.
     * 
     * @param demande
     * @return
     */
    private DemandeDTO processDemandeFromSystemeTiers(DemandeDTO demande) {
        // Injection de la config avant retour au FO
        demande.setConfig(demandesConfigService.getConfig(demande.getBuildId()).getContenu());
        
        // Injection d'une meta BACK_FRONT_* si pas de meta ou si présente mais pas BACK_FRONT_* ni FRONT_IDX_*
        // Ceci afin de se prémunir d'une erreur lors de l'affichage de la demande dans le FO
        // Cf. ticket https://redmine.monaco-gouvernement.mc/issues/71709
        for (DemandeFileDTO file : demande.getFichiers()) {
            
            if (StringUtils.isBlank(file.getMeta()) || (!StringUtils.isBlank(file.getMeta()) && !file.getMeta().startsWith(FileUtils.META_BACK_FRONT) && !file.getMeta().startsWith(FileUtils.META_FRONT_IDX))) {
                file.setMeta(FileUtils.META_BACK_FRONT_SYSTEME_TIERS);
            }
        }
        
        return demande;
    }

}

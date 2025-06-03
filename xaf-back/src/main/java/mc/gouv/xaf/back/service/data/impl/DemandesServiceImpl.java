package mc.gouv.xaf.back.service.data.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import mc.gouv.xaf.back.data.dao.AccessRepository;
import mc.gouv.xaf.back.data.dao.DemandesAgentsRepository;
import mc.gouv.xaf.back.data.dao.DemandesCommentaireRepository;
import mc.gouv.xaf.back.data.dao.DemandesComplementsRepository;
import mc.gouv.xaf.back.data.dao.DemandesHistoriqueRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesUsagersRepository;
import mc.gouv.xaf.back.data.dao.PurgeFilesRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.data.entity.DemandesAgentsBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsBO;
import mc.gouv.xaf.back.data.entity.DemandesHistoriqueBO;
import mc.gouv.xaf.back.data.entity.DemandesUsagersBO;
import mc.gouv.xaf.back.data.model.ErrorEventDTO;
import mc.gouv.xaf.back.data.projection.DemandeExportProjection;
import mc.gouv.xaf.back.data.transformer.DemandesAgentsTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemandeFilesCategorizer;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.DemandesComplementsService;
import mc.gouv.xaf.back.service.data.DemandesConfigService;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.data.MarqueursService;
import mc.gouv.xaf.back.service.data.StatistiquesService;
import mc.gouv.xaf.back.service.excel.AfDemandeExcelFlatIterable;
import mc.gouv.xaf.back.service.excel.AfExcelExportModelProvider;
import mc.gouv.xaf.back.service.handlers.TransactionErrorsHandler;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.DemandeRecapDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import mc.gouv.xaf.back.service.itg.nomen.PaysCache;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.back.service.pdf.impl.AfPdfTemplateAndModelProvider;
import mc.gouv.xaf.back.service.postprocessing.AfPostProcessingProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.back.service.utils.RechercheDemandesUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.AfDemandeExcelFlatDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import mc.gouv.xaf.shared.dto.DonneesMConnectDTO;
import mc.gouv.xaf.shared.dto.MarqueurDTO;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import mc.gouv.xaf.shared.dto.StatistiqueDTO;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import mc.gouv.xaf.shared.enums.DemandeComplementsStatutEnum;
import mc.gouv.xaf.shared.enums.TypeConnexionUsagerEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.velocity.tools.generic.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des demandes.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesServiceImpl implements DemandesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesServiceImpl.class);
    private static final String RECUPERATION_DEMANDES = "Récupération en base des demandes...";
    private static final String RECUPERATION_DEMANDE = "Récupération en base de la demande...";

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private DemandesAgentsRepository demandesAgentsRepository;

    @Autowired
    private DemandesUsagersRepository demandesUsagersRepository;

    @Autowired
    private AccessRepository accessRepository;

    @Autowired
    private AccessService accessService;

    @Autowired
    private PurgeFilesRepository purgeFilesRepository;

    @Autowired
    private AfPostProcessingProvider afPostProcessingProvider;

    @Autowired
    private DemandesStatutsService demandesStatutsService;

    @Autowired
    private DemandesHistoriqueRepository demandesHistoriqueRepository;

    @Autowired
    private DemandesCommentaireRepository demandesCommentaireRepository;

    @Autowired
    private DemarchesService demarchesService;

    @Autowired
    private FileService fileService;

    @Autowired
    private DemandesFilesService demandesFilesService;

    @Autowired
    private DemandesConfigService demandesConfigService;

    @Autowired
    private DemandesComplementsService demandesComplementsService;

    @Autowired
    private DemandesDataService demandesDataService;

    @Autowired
    private StatistiquesService statistiquesService;

    @Autowired
    private GUKafkaProducer guKafkaProducer;

    @Autowired
    private GUKafkaUtils guKafkaUtils;

    @Autowired
    private DemandesTransformer demandesTransformer;

    @Autowired
    private DemandesAgentsTransformer demandesAgentsTransformer;

    @Autowired
    private UtilisateursCache utilisateursCache;

    @Autowired
    private PaysCache paysCache;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private RechercheDemandesUtils rechercheDemandesUtils;

    @Autowired
    private DemandesComplementsRepository demandesComplementsRepository;

    @Autowired
    private AfExcelExportModelProvider excelExportModelProvider;

    @Autowired
    private AfPdfTemplateAndModelProvider afPdfTemplateAndModelProvider;

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private MotifsCache motifsCache;

    @Autowired
    private DemandeFilesCategorizer demandeFilesCategorizer;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @Autowired
    private MarqueursService marqueursService;

    @Autowired
    private TransactionErrorsHandler transactionErrorsHandler;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private String generatePublicIDWithoutCollisionCheck(String prefixe) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String stringDate = dateFormat.format(new Date());
        SecureRandom random = new SecureRandom();
        String randomPart = new BigInteger(130, random).toString(32).substring(0, 4);
        String ret = prefixe + "-" + stringDate + "-" + randomPart;
        return ret.toUpperCase();
    }

    /**
     * Permet de générer l'ID public d'une demande idPrefix-yyyyMMdd-randomAlphaNumerique(4) Exemple :
     * HAB-20161014-n6kd
     */
    private String generatePublicID() {
        LOGGER.info("Récupération du préfixe d'identifiant depuis la démarche associée...");
        String prefixe = demarchesService.getDemarche().getIdentifiantPrefixe();

        // Génération de l'identifiant de la demande (ID public)
        String identifiant = generatePublicIDWithoutCollisionCheck(prefixe);
        LOGGER.info("Identifiant généré : {}", identifiant);

        // Puis on s'assure que cet ID généré n'existe pas déjà (extrêmement rare, mais on fait la vérification quand même)
        while (demandesRepository.findByIdentifiant(identifiant) != null) {
            identifiant = generatePublicIDWithoutCollisionCheck(prefixe);
            LOGGER.info("COLLISION : génération d'un nouvel identifiant : {}", identifiant);
        }
        return identifiant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDTO saveDemande(DemandeDTO demande, String premierStatutName, JsonNode donneesExternes) {
        try {
            if (demande.getCanal() == null) {
                throw new DemarchesServiceException("Canal non spécifié", HttpStatus.BAD_REQUEST);
            }
            
            LOGGER.info("Récupération en base de l'accès correspondant...");
            AccessBO accessBo = accessService.getAccessBOActive(demande.getUsagerId());

            LOGGER.info("Postprocessing de la demande...");
            try {
                demande = afPostProcessingProvider.postprocess(demande, donneesExternes);
            } catch (Exception e) {
                LOGGER.error("Une erreur est survenue lors du postprocessing de la demande", e);
            }

            if (demande.getFichiers() != null) {
                for (DemandeFileDTO file : demande.getFichiers()) {
                    file.setDate(new Date());
                }
            }

            demande.setDateCreation(new Date());
            demande.setDateDerModif(demande.getDateCreation());

            // Génération de l'identifiant de la demande (ID public)
            String identifiant = generatePublicID();
            demande.setIdentifiant(identifiant);

            // Création d'une nouvelle demande, ignorer les champs suivants (ils seront mis à jour plus tard lors du traitement d'une demande) :
            demande.setObservations(null);
            demande.setTypeConnexionUsager(AfBackUtils.getTypeConnexion(demande));

            LOGGER.info(SharedMessages.TRANSFORMATION_DTO_BO);
            DemandeBO demandeBo = demandesTransformer.dto2Bo(demande);
            demandeBo.setFkAccess(accessBo);

            LOGGER.info("Récupération en base de l'user correspondant...");
            DemandesUsagersBO usagerBO = demandesUsagersRepository.findOneById(demande.getUsagerId());
            if (usagerBO != null) {
                // si l'usager existe déjà on le réutilise
                demandeBo.setUsager(usagerBO);
            }

            // on utilise la dernière config déjà présente en base
            DemandeConfigBO config = demandesConfigService.getLastConfig();
            demandeBo.setConfig(config);

            // set contenuTrad
            JsonNode contenuTrad = demande.getContenu().deepCopy();
            setContenuTrad(contenuTrad, config.getContenu());
            demandeBo.setContenuTrad(contenuTrad);

            LOGGER.info(SharedMessages.SAUVEGARDE_EN_BASE);
            demandeBo = demandesRepository.save(demandeBo);

            // Maintenant on s'occupe d'attacher et de persister les pièces jointes...
            demandesFilesService.saveFiles(demande.getFichiers(), demandeBo);

            // Créer le premier statut de la demande
            LOGGER.info("Création d'un statut \"{}\" pour la demande...", premierStatutName);
            DemandeDTO demandeDTO = demandesStatutsService.updateStatut(demandeBo, premierStatutName, null,
                    demandeBo.getFkAccess().getUsagerId(), null, null, null);

            // Lier les fichiers de la demande au DemandeID, dans FILE
            if (demande.getFichiers() != null) {
                LOGGER.info("Lier ces fichiers au DemandeID dans FILE...");
                fileService.updateFilesMetadataWithDemandeId(demande.getFichiers(), demandeBo.getPkDemandes());
            }

            return demandeDTO;
        } catch (Exception e) {
            LOGGER.error("Erreur lors de saveDemande");
            ErrorEventDTO esErrorEventDTO = transactionErrorsHandler.createErrorEvent(
                    "DemandesServiceImpl - méthode saveDemande()", e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void setContenuTrad(JsonNode contenuTrad, JsonNode config) {
        JsonNode mappings = config.get("mappings");
        List<JsonNode> champsNodes = config.get("recap").findValues("champs");
        for (JsonNode champs : champsNodes) {
            for (JsonNode champ : champs) {
                if (!champ.get("type").asText().equals("tableau")) {
                    JsonNode mapping = champ.get("mapping");
                    String path = champ.get("path").asText();
                    processContenuTrad(contenuTrad, mappings, mapping, champ, path);
                }
            }
        }
        // récupérer aussi les champs tableau
        List<JsonNode> tableauxNodes = new ArrayList<>();
        extractTableauNodes(config.get("recap"), tableauxNodes);
        for (JsonNode tableau : tableauxNodes) {
            String rootPath = tableau.get("path").asText();
            // on regarde si dans le contenu on a une array correspondant à ce path
            JsonNode array = AfBackUtils.getNodeFromPath(contenuTrad, rootPath);
            for (JsonNode champ : tableau.get("columns")) {
                JsonNode mapping = champ.get("mapping");
                // il faut itérer sur chaque contenu du tableau
                for (int i = 0; i < array.size(); i++) {
                    String path = rootPath + "." + i + "." + champ.get("path").asText();
                    processContenuTrad(contenuTrad, mappings, mapping, champ, path);
                }

            }
        }
    }

    private void extractTableauNodes(JsonNode node, List<JsonNode> tableauNodes) {
        if (node.isObject()) {
            // Si le nœud est un objet JSON
            JsonNode columnsNode = node.get("columns");
            if (columnsNode != null && columnsNode.isArray()) {
                tableauNodes.add(node);
            }

            // Parcourir les enfants de l'objet
            node.fields().forEachRemaining(entry -> extractTableauNodes(entry.getValue(), tableauNodes));
        } else if (node.isArray()) {
            // Si le nœud est un tableau
            node.forEach(childNode -> extractTableauNodes(childNode, tableauNodes));
        }
    }

    private void processContenuTrad(JsonNode contenuTrad, JsonNode mappings, JsonNode mapping, JsonNode champ,
            String path) {
        // le champ a un mapping
        if (mapping != null) {
            // on récupère le champ correspondant dans le contenu s'il existe
            JsonNode enumKeyNode = AfBackUtils.getNodeFromPath(contenuTrad, path);
            if (enumKeyNode != null && !enumKeyNode.isNull()) {
                if (enumKeyNode.isArray()) {
                    // choix multiple
                    ObjectMapper objectMapper = new ObjectMapper();
                    ArrayNode arrayNodeValues = objectMapper.createArrayNode();
                    for (JsonNode element : enumKeyNode) {
                        String enumValue;
                        String enumKey = element.asText();
                        JsonNode enumFound = mappings.get(mapping.asText()).get("languages").get("fr").get("values")
                                .get(enumKey);
                        if (enumFound != null) {
                            // si on trouve l'enum, alors on récupère la valeur
                            enumValue = enumFound.asText();
                        } else {
                            // sinon cela veut dire que la traduction a déjà été effectuée du coup on peut réutiliser la valeur
                            enumValue = enumKey;
                        }
                        arrayNodeValues.add(enumValue);
                    }
                    AfBackUtils.setNodeValueArray(contenuTrad, path, arrayNodeValues);
                } else {
                    // choix
                    String enumValue = "";
                    String enumKey = enumKeyNode.asText();
                    JsonNode isDynamic = champ.get("isDynamic");
                    if (isDynamic != null && !isDynamic.asBoolean()) {
                        JsonNode enumFound = mappings.get(mapping.asText()).get("languages").get("fr").get("values")
                                .get(enumKey);
                        if (enumFound != null) {
                            // si on trouve l'enum, alors on récupère la valeur
                            enumValue = enumFound.asText();
                        } else {
                            // sinon cela veut dire que la traduction a déjà été effectuée du coup on peut réutiliser la valeur
                            enumValue = enumKey;
                        }
                    } else if (mapping.asText().equals("nationalites")) {
                        enumValue = StringUtils.isBlank(enumKey) ? ""
                                : paysCache.get(enumKey) != null ? paysCache.get(enumKey).getNationalite() : enumKey;
                    } else if (mapping.asText().equals("pays")) {
                        enumValue = StringUtils.isBlank(enumKey) ? ""
                                : paysCache.get(enumKey) != null ? paysCache.get(enumKey).getLibelle() : enumKey;
                    }
                    AfBackUtils.setNodeValue(contenuTrad, path, enumValue);
                }
            }
        } else if (champ.get("type").asText().equals("adresse")) {
            // le champ est de type adresse donc on doit remplacer le pays
            path += ".pays";
            JsonNode enumKeyNode = AfBackUtils.getNodeFromPath(contenuTrad, path);
            if (enumKeyNode != null && !enumKeyNode.isNull() && !enumKeyNode.isMissingNode()) {
                String enumKey = enumKeyNode.asText();
                String enumValue = StringUtils.isBlank(enumKey) ? ""
                        : paysCache.get(enumKey) != null ? paysCache.get(enumKey).getLibelle() : enumKey;
                AfBackUtils.setNodeValue(contenuTrad, path, enumValue);
            }
        } else if (champ.get("type").asText().equals("date")) {
            JsonNode dateNode = AfBackUtils.getNodeFromPath(contenuTrad, path);
            if (dateNode != null && !dateNode.isNull()) {
                String date = dateNode.asText();
                // Si la date a un format d'affichage
                String format = "dd/MM/yyyy";
                JsonNode formatNode = champ.get("displayJavaFormat");
                if (formatNode != null && !formatNode.isNull()) {
                    format = formatNode.asText();
                }
                AfBackUtils.setNodeValue(contenuTrad, path, AfBackUtils.changeDateStringFormat(format, date));
            }
        }
    }

    @Override
    public DemandeDTO saveOrUpdateDemande(DemandeDTO demande, boolean partialUpdate, String premierStatutName,
            JsonNode donneesExternes) {
        DemandeDTO demandeDTO;
        if (demande.getPkDemandes() != null) {
            // ID de la demande fourni, il faut donc mettre à jour une demande
            demandeDTO = updateDemande(demande, partialUpdate);
        } else {
            // UsagerID fournis, il faut donc créer une nouvelle demande
            demandeDTO = saveDemande(demande, premierStatutName, donneesExternes);
        }
        return demandeDTO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeDTO> getDemandes(Integer usagerId) {
        return getDemandesUsager(usagerId);
    }

    @Override
    public List<DemandeDTO> getDemandesUsagerDesinscription(Integer usagerId) {
        LOGGER.info(RECUPERATION_DEMANDES);
        AccessBO accessBo = accessService.getAccessBO(usagerId, true);
        if (accessBo == null) {
            throw new DemarchesServiceException("Accès correspondant introuvable", HttpStatus.NOT_FOUND);
        }
        return demandesRepository.findByUsagerId(usagerId).stream()
                .map(demande -> demandesTransformer.lightProjection2Dto(demande)).toList();
    }

    private List<DemandeDTO> getDemandesUsager(Integer usagerId) {
        LOGGER.info(RECUPERATION_DEMANDES);
        AccessBO accessBo = accessService.getAccessBO(usagerId, true);
        if (accessBo == null) {
            throw new DemarchesServiceException("Accès correspondant introuvable", HttpStatus.NOT_FOUND);
        }
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return demandesTransformer.bo2Dto(new ArrayList<>(accessBo.getDemandes()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeDTO> getDemandesFilterFiles(Integer usagerId) {
        List<DemandeDTO> demandes = getDemandes(usagerId);
        for (DemandeDTO demande : demandes) {
            DemandeFileDTO[] fichiers = demande.getFichiers();
            if (fichiers != null) {
                demande.setFichiers(DemarchesUtils.filterFiles(fichiers));
            }
        }
        return demandes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeDTO> getDemandesByIdentifiants(List<String> identifiants) {
        LOGGER.info(RECUPERATION_DEMANDES);
        List<DemandeBO> demandes = demandesRepository.findAllByIdentifiantIn(identifiants);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return demandesTransformer.bo2Dto(demandes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeDTO> getDemandes() {

        LOGGER.info(RECUPERATION_DEMANDES);

        // Si usagerId null, alors rechercher tous les accès qui sont actifs
        ArrayList<DemandeBO> demandes = new ArrayList<>();
        List<AccessBO> accessBos = accessRepository.findByActive(true);
        for (AccessBO access : accessBos) {
            demandes.addAll(access.getDemandes());
        }

        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return demandesTransformer.bo2Dto(demandes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<AfDemandeExcelFlatDTO> getAllDemandesFilteredByDateAndStatut(Pageable pageable, Date startDate,
            Date endDate,
            String statut) {
        Page<DemandeExportProjection> demandesPage;
        if (statut == null) {
            if (startDate != null && endDate != null) {
                demandesPage = demandesRepository.findByDateCreationBetween(pageable, startDate, endDate);
            } else if (startDate != null) {
                demandesPage = demandesRepository.findByDateCreationGreaterThanEqual(pageable, startDate);
            } else if (endDate != null) {
                demandesPage = demandesRepository.findByDateCreationLessThanEqual(pageable, endDate);
            } else {
                demandesPage = demandesRepository.findAllBy(pageable);
            }
        } else {
            if (startDate != null && endDate != null) {
                demandesPage = demandesRepository.findByDateCreationBetweenAndDernierStatut_Name(pageable, startDate,
                        endDate, statut);
            } else if (startDate != null) {
                demandesPage = demandesRepository.findByDateCreationGreaterThanEqualAndDernierStatut_Name(pageable,
                        startDate, statut);
            } else if (endDate != null) {
                demandesPage = demandesRepository.findByDateCreationLessThanEqualAndDernierStatut_Name(pageable,
                        endDate, statut);
            } else {
                demandesPage = demandesRepository.findAllByDernierStatut_Name(pageable, statut);
            }
        }

        return demandesPage.map(demande -> {
            DemandeDTO dto = demandesTransformer.exportProjection2Dto(demande);
            // pour des questions de performances et éviter l'effet n+1 sur le onetomany, on doit récupérer les data dans un second temps
            dto.setData(demandesDataService.getDemandeDatasProjection(demande.getPkDemandes()));
            return excelExportModelProvider.getDemandeFlat(dto);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeDTO> getAllDemandesFilteredByStatut(String statut) {
        List<DemandeBO> demandes = demandesRepository.findAllByDernierStatut_Name(statut);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return demandesTransformer.bo2Dto(demandes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeDTO> getAllDemandesFilteredByStatuts(List<String> statuts) {
        return demandesRepository.findAllByDernierStatut_NameIn(statuts).stream()
                .map(demande -> demandesTransformer.lightProjection2Dto(demande)).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeDTO> getAllDemandesFilteredByStatutAndDateDernierStatut(String statut, Date date) {
        List<DemandeBO> demandes = demandesRepository.findAllByDernierStatut_NameAndDernierStatutDateLessThan(statut,
                date);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return demandesTransformer.bo2Dto(demandes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeBO getCheckDemarcheDemandeBO(DemandeDTO demande, boolean checkActive) {
        return getCheckDemarcheDemandeBO(demande.getPkDemandes(), checkActive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeBO getCheckDemarcheDemandeBO(Integer demandeId, boolean checkActive) {

        LOGGER.debug(RECUPERATION_DEMANDE);

        Optional<DemandeBO> demandeBoOp = demandesRepository.findById(demandeId);

        // Gérer les accès désactivés
        if (demandeBoOp.isPresent() && !demandeBoOp.get().getFkAccess().isActive() && DemarchesUtils.isFrontUser()
                && checkActive) {
            demandeBoOp = Optional.empty();
        }

        if (demandeBoOp.isEmpty()) {
            LOGGER.error("Le demande ID: {}, est introuvable.", demandeId);
            throw new DemarchesServiceException("Demande introuvable ou supprimée", HttpStatus.NOT_FOUND);
        }

        return demandeBoOp.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDTO getCheckDemarcheDemandeDTO(Integer demandeId, boolean checkActive) {
        DemandeBO demandeBo = getCheckDemarcheDemandeBO(demandeId, checkActive);
        if (demandeBo == null) {
            return null;
        }
        return demandesTransformer.bo2Dto(demandeBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDTO getDemande(Integer pkDemande, Integer usagerId) {
        LOGGER.info(RECUPERATION_DEMANDE);
        DemandeBO demandeBo = demandesRepository.findByFkAccess_UsagerIdAndPkDemandesAndFkAccess_ActiveTrue(usagerId,
                pkDemande);
        if (demandeBo == null) {
            throw new DemarchesServiceException(SharedMessages.DONNEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return demandesTransformer.bo2Dto(demandeBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDTO getDemandeFilterFiles(Integer pkDemande, Integer usagerId) {
        DemandeDTO demande = getDemande(pkDemande, usagerId);
        DemandeFileDTO[] fichiers = demande.getFichiers();
        if (fichiers != null) {
            demande.setFichiers(DemarchesUtils.filterFiles(fichiers));
        }
        return demande;
    }

    @Override
    public byte[] getDemandeRecap(Integer pkDemande, Integer usagerId, DonneesMConnectDTO donneesMConnectDTO) {
        DemandeDTO demande = getDemande(pkDemande, usagerId);
        demande.setDonneesMConnect(donneesMConnectDTO);
        demandesTransformer.hideDernierStatut(demande);
        // transformer les complements pour affichage
        if (demande.getComplements() != null) {
            for (DemandeComplementsDTO demandeComplementsDTO : demande.getComplements()) {
                String codeMotif = demandeComplementsDTO.getQuestion().getCodeMotif();
                demandeComplementsDTO.getQuestion().setCodeMotif(motifsCache.getMotif(codeMotif, "fr").getLibelle());
            }
        }
        // transformer le motif
        String codeMotif = demande.getDernierStatut().getCodeMotif();
        if (codeMotif != null) {
            demande.getDernierStatut().setCodeMotif(motifsCache.getMotif(codeMotif, "fr").getLibelle());
        }
        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                InputStream in = this.getClass().getResourceAsStream("/pdfrecap/DemandeRecap.docx")) {

            IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Velocity);

            LOGGER.info("Création du contexte avec le modèle fourni par la démarche...");
            IContext context = report.createContext();
            for (Entry<String, Object> entry : afPdfTemplateAndModelProvider.getGenericModelPdf(demande).entrySet()) {
                context.put(entry.getKey(), entry.getValue());
            }
            context.put("demande", demande);
            context.put("Utils", AfBackUtils.class);
            context.put("date", new DateTool());
            context.put("mconnect", donneesMConnectDTO != null);
            List<String> marqueursForRecap = demarchesDataProvider.getMarqueursForRecap();
            Map<String, String> map = null;
            if (marqueursForRecap != null) {
                map = new HashMap<>();
                for (String identifiant : marqueursForRecap) {
                    MarqueurDTO marqueur = marqueursService.getMarqueur(demande.getConfig().get("buildId").asText(),
                            identifiant);
                    if (marqueur != null) {
                        String valeur;
                        if ("choixMultiple".equals(marqueur.getType())) {
                            List<String> choix = demande.getMarqueurChoixMultipleTrad(identifiant);
                            valeur = String.join(", ", choix);
                        } else {
                            valeur = demande.getMarqueurTrad(identifiant);
                        }
                        map.put(marqueur.getDescription(), valeur);
                    }
                }
            }
            context.put("infosDemande", map);
            // hack rescart/resprim pour récupérer le nom du statut dans les trad du config.json
            context.put("dernierStatut", demarchesDataProvider.getLibelleDernierStatut(demande));

            Options options = Options.getTo(ConverterTypeTo.PDF);

            report.convert(context, options, bos);
            bytes = bos.toByteArray();
        } catch (IOException | XDocReportException e) {
            throw new DemarchesServiceException("Erreur lors de la génération", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }

        try (PDDocument mainDocument = Loader.loadPDF(bytes);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDFMergerUtility mergerUtility = new PDFMergerUtility();

            // Ajouter les PDFs des URLs
            for (DemandeFileDTO file : demandeFilesCategorizer.fichiersFront(demande.getFichiers())) {
                try (InputStream inputStream = fileService.getFile(
                        gouvPropertiesResolver.getDemarcheId() + "/" + gouvPropertiesResolver.getContainerId() + "/"
                                + file.getUrl()); PDDocument urlPdf = Loader.loadPDF(inputStream.readAllBytes())) {
                    mergerUtility.appendDocument(mainDocument, urlPdf);
                }
            }

            // Sauvegarder le document fusionné en byte[]
            mainDocument.save(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new DemarchesServiceException("Erreur lors de la génération", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDTO getDemande(Integer pkDemandes) {
        DemandeBO demandeBo = getCheckDemarcheDemandeBO(pkDemandes, true);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return demandesTransformer.bo2Dto(demandeBo);
    }

    @Override
    public DemandeDTO updateDemande(DemandeDTO demande, boolean partialUpdate) {
        try {
            DemandeBO demandeBo = getCheckDemarcheDemandeBO(demande, true);

            // Mise à jour du contenu
            setContenu(demandeBo, demande, partialUpdate);

            // Mise à jour du contenu initial
            setContenuInitial(demande, partialUpdate, demandeBo);

            // Mise à jour du timestamp pour verrouillage
            demandeBo.setModificationTimestamp(demande.getModificationTimestamp());

            // Mise à jour des observations
            if (!partialUpdate || demande.getObservations() != null) {
                demandeBo.setObservations(demande.getObservations());
            }
            if (demande.getAgent() != null) {
                User user = utilisateursCache.get(demande.getAgent().getId());
                demandeBo.setAgent(demandesAgentsTransformer.user2Bo(user));
            }

            // Mise à jour du canal
            if (!partialUpdate && demande.getCanal() != null) {
                demandeBo.setCanal(demande.getCanal().name());
            }

            // Mise à jour de la date de dernière modification
            demandeBo.setDateDerModif(new Date());

            // Supprimer les pièces jointes déjà existantes
            if (!partialUpdate || demande.getFichiers() != null) {
                demandesFilesService.updateFichiers(demandeBo, demande.getFichiers());
            }

            demandeBo = demandesRepository.save(demandeBo);

            LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
            DemandeDTO dto = demandesTransformer.bo2Dto(demandeBo);
            dto.setUpdated(true);
            return dto;
        } catch (Exception e) {
            LOGGER.error("Erreur lors de updateDemande");
            ErrorEventDTO esErrorEventDTO = transactionErrorsHandler.createErrorEvent(
                    "DemandesServiceImpl - méthode updateDemande()", demande.getPkDemandes(), e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void setContenu(DemandeBO demandeBo, DemandeDTO demande, boolean partialUpdate) {
        if (!partialUpdate || demande.getContenu() != null && !demande.getContenu().isNull()) {
            demandeBo.setContenu(demande.getContenu());
            // set contenuTrad
            JsonNode contenuTrad = demande.getContenu().deepCopy();
            setContenuTrad(contenuTrad, demandeBo.getConfig().getContenu());
            demandeBo.setContenuTrad(contenuTrad);
        }
    }

    private void setContenuInitial(DemandeDTO demande, boolean partialUpdate, DemandeBO demandeBo) {
        if (!partialUpdate || demande.getContenuInitial() != null && !demande.getContenuInitial().isNull()) {
            demandeBo.setContenuInitial(demande.getContenuInitial());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteDemande(Integer demandeId) {
        try {
            DemandeBO demandeBo = getCheckDemarcheDemandeBO(demandeId, false);
            if (demandeBo == null) {
                throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
            }

            DemandeDTO demandeDTO = demandesTransformer.bo2Dto(demandeBo);
            LOGGER.info("Suppression des fichiers de la demande {}...", demandeId);
            demandesFilesService.suppressionDesFichiers(demandeDTO);
            LOGGER.info("Suppression des fichiers complémentaires de la demande {}...", demandeId);
            demandesComplementsService.suppressionDesFichiersDesDemandesComplementaires(demandeDTO, false, null, 0);

            AccessBO access = suppressionDeLaDemande(demandeBo, demandeId);

            String identifiant = demandeBo.getIdentifiant();
            Date dateCreation = demandeBo.getDateCreation();
            LOGGER.info(
                    "Envoi d'un message dans Kafka pour notifier le Guichet Unique de la suppression de la demande...");
            List<DemandeRecapDTO> demandeRecaps = guKafkaUtils.getDemandeRecapsFromUsagerId(demandeDTO.getUsagerId());
            RecapDemandesDTO recapDemandes = guKafkaUtils.getRecapDemandes(demandeRecaps);
            guKafkaProducer.sendSuppressionDemandeMessage(access.getUsagerId(), demandeId, identifiant, dateCreation,
                    recapDemandes);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de deleteDemande");
            ErrorEventDTO esErrorEventDTO = transactionErrorsHandler.createErrorEvent(
                    "DemandesServiceImpl - méthode deleteDemande()", demandeId, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private AccessBO suppressionDeLaDemande(DemandeBO demandeBo, Integer demandeId) {
        StatistiqueDTO stat = new StatistiqueDTO();
        stat.setCanal(demandeBo.getCanal());
        stat.setDate(new Date());
        stat.setDemandeId(demandeId);
        stat.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
        stat.setIdentifiantDemande(demandeBo.getIdentifiant());
        stat.setStatutPublic(AfBackUtils.STATUT_PUBLIC_SUPPRIMEE);
        if (!StringUtils.isEmpty(demandeBo.getTypeConnexionUsager())) {
            stat.setTypeConnexionUsager(TypeConnexionUsagerEnum.valueOf(demandeBo.getTypeConnexionUsager()));
        }
        AccessBO access = demandeBo.getFkAccess();
        access.getDemandes().remove(demandeBo);
        access = accessRepository.save(access);

        // Suppression de l'historique de la demande (pas géré par cascade, donc le faire ici)
        LOGGER.info("Suppression de l'historique de la demande...");
        List<DemandesHistoriqueBO> histos = demandesHistoriqueRepository.findByFkDemandesPkDemandes(demandeId);
        demandesHistoriqueRepository.deleteAll(histos);

        LOGGER.info("Ajout d'une ligne de statistique pour la suppression de la demande...");
        statistiquesService.saveStatistique(stat);

        LOGGER.info("Suppression de la demande {}...", demandeId);
        demandesRepository.delete(demandeBo);

        return access;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteDemandeInGivenStatus(Integer demandeId, List<String> statuts, int jours) {
        try {
            LOGGER.info("Suppression de la demande {}...", demandeId);
            DemandeBO demandeBo = getCheckDemarcheDemandeBO(demandeId, false);
            if (demandeBo == null) {
                throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
            }
            AccessBO access = demandeBo.getFkAccess();

            /*** Insertion de statistique */
            LOGGER.info("Ajout d'une ligne de statistique pour la suppression de la demande...");
            StatistiqueDTO stat = new StatistiqueDTO();
            stat.setCanal(demandeBo.getCanal());
            stat.setDate(new Date());
            stat.setDemandeId(demandeId);
            stat.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
            stat.setIdentifiantDemande(demandeBo.getIdentifiant());
            stat.setStatutPublic(AfBackUtils.STATUT_PUBLIC_SUPPRIMEE);
            statistiquesService.saveStatistique(stat);

            // Suppression de l'historique de la demande (pas géré par cascade, donc le faire ici)
            LOGGER.info("Suppression de l'historique de la demande...");
            demandesHistoriqueRepository.deleteByFkDemandesPkDemandes(demandeId);
            // Suppression des commentaires de la demande (pas géré par cascade, donc le faire ici)
            LOGGER.info("Suppression des commentaires de la demande...");
            demandesCommentaireRepository.deleteByFkDemandesPkDemandes(demandeId);

            /*** Sauvegarde des fichiers à purger avant suppression de la demande. */
            /*** Les fichiers et compléments sont supprimés en cascade des tables liées à la suppression de la demande */
            LOGGER.info("insertion des fichiers à purger dans la table pour la demande: {}", demandeId);
            purgeFilesRepository.insertFilesToPurge(demandeId);
            purgeFilesRepository.insertFilesComplementsToPurge(demandeId);
            purgeFilesRepository.insertFilesCourrierToPurge(demandeId);

            DemandesAgentsBO agent = demandeBo.getAgent();
            DemandesUsagersBO usager = demandeBo.getUsager();
            /*** Suppression de la demande. */
            LOGGER.info("Appel du répo pour la suppression...");
            demandesRepository.delete(demandeBo);

            // Suppression de l'agent (pas géré par cascade, donc le faire ici)
            LOGGER.info("Vérification de l'agent");
            if (agent != null && !demandesRepository.existsByAgent(agent)) {
                LOGGER.info("L'agent associé n'est pas utilisé ailleurs, suppression...");
                demandesAgentsRepository.delete(agent);
            }
            // Suppression de l'usager (pas géré par cascade, donc le faire ici)
            LOGGER.info("Vérification de l'usager");
            if (!demandesRepository.existsByUsager(usager)) {
                LOGGER.info("L'usager associé n'est pas utilisé ailleurs, suppression...");
                demandesUsagersRepository.delete(usager);
            }

            LOGGER.info(
                    "Envoi d'un message dans Kafka pour notifier le Guichet Unique de la suppression de la demande...");
            List<DemandeRecapDTO> demandeRecaps = guKafkaUtils.getDemandeRecapsFromUsagerId(access.getUsagerId());
            RecapDemandesDTO recapDemandes = guKafkaUtils.getRecapDemandes(demandeRecaps);
            guKafkaProducer.sendSuppressionDemandeMessage(access.getUsagerId(), demandeId, demandeBo.getIdentifiant(),
                    demandeBo.getDateCreation(), recapDemandes);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de deleteDemandeInGivenStatus");
            ErrorEventDTO esErrorEventDTO = transactionErrorsHandler.createErrorEvent(
                    "DemandesServiceImpl - méthode deleteDemandeInGivenStatus()", demandeId, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Integer getAccessIdFromDemande(DemandeDTO demande) {
        return getCheckDemarcheDemandeBO(demande, true).getFkAccess().getPkAccess();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDTO cloneDemande(Integer pkDemande) {
        try {
            DemandeBO demandeBo = getCheckDemarcheDemandeBO(pkDemande, true);

            LOGGER.info("Duplication de la demande...");
            DemandeDTO demandeDto = demandesTransformer.bo2Dto(demandeBo);
            DemandeBO newDemandeBo = demandesTransformer.dto2Bo(demandeDto);
            newDemandeBo.setFkAccess(demandeBo.getFkAccess());
            newDemandeBo.setPkDemandes(null);
            newDemandeBo.setRecapType(demandeBo.getRecapType());
            newDemandeBo.setDonneesCertifiees(demandeBo.getDonneesCertifiees());
            newDemandeBo.setConfig(demandeBo.getConfig());
            newDemandeBo.setTypeConnexionUsager(demandeBo.getTypeConnexionUsager());
            // #4840 Enlever l'affectation
            newDemandeBo.setAgent(null);
            newDemandeBo.setUsager(demandeBo.getUsager());
            newDemandeBo = demandesRepository.save(newDemandeBo);

            // Pièces jointes des demandes
            demandesFilesService.clonerDesPiecesJointes(demandeBo, newDemandeBo);

            // Demandes d'informations complémentaires des demandes
            demandesComplementsService.clonerDemandeComplements(demandeBo, newDemandeBo);

            // Data des demandes
            demandesDataService.clonerDemandeData(demandeBo, newDemandeBo);

            // Génération d'un nouvel identifiant de demande
            String identifiant = generatePublicID();
            newDemandeBo.setIdentifiant(identifiant);

            newDemandeBo = demandesRepository.save(newDemandeBo);

            LOGGER.info("Duplication terminée");

            // On passe tous les demandes complements à repondue pour la demande dupliquée
            // cf #66472 - [INCIDENT] [BO] erreur 500 sur demande d'info comp sur une demande annulée et dupliquée
            for (DemandesComplementsBO compl : newDemandeBo.getDemandesComplements()) {
                compl.setStatut(DemandeComplementsStatutEnum.REPONDUE.name());
                demandesComplementsRepository.save(compl);
                LOGGER.info("Passage de l'info compl : {} à répondue car duplication de la demande {}",
                        compl.getPkDemandesComplements(), pkDemande);
            }

            return demandesTransformer.bo2Dto(newDemandeBo);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de cloneDemande");
            ErrorEventDTO esErrorEventDTO = transactionErrorsHandler.createErrorEvent(
                    "DemandesServiceImpl - méthode cloneDemande()", pkDemande, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche, Pageable pageable, String[] fields) {

        Long totalCount = rechercheDemandesUtils.getDemandesCount(demandeRecherche);
        List<DemandeBO> demandes = rechercheDemandesUtils.getDemandesPageable(demandeRecherche, pageable);
        List<DemandeDTO> demandesDto = demandesTransformer.bo2Dto(demandes, fields);

        return new PageImpl<>(demandesDto, pageable, totalCount);
    }

    @Override
    public mc.gouv.xaf.shared.dto.Page<DemandeDTO> getDemandesPageable(Integer usagerId, String[] status,
            PageParamDTO paramDTO) {
        String sortColumn = paramDTO.getSort();
        Sort sort = "DESC".equals(paramDTO.getDirection()) ? Sort.by(sortColumn).descending() : Sort.by(sortColumn);
        Pageable pageable = PageRequest.of(paramDTO.getPage(), paramDTO.getSize(), sort);
        Page<DemandeBO> bos = demandesRepository.findByUsagerIdAndStatuts(usagerId, status, paramDTO.getLang(),
                pageable);
        return demandesTransformer.boPage2DtoPage(bos);
    }

    @Override
    public List<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche) {
        List<DemandeBO> demandes = rechercheDemandesUtils.getDemandes(demandeRecherche);
        return demandesTransformer.bo2Dto(demandes);
    }

    @Override
    public DemandeDTO associerDemandeCourrier(Integer pkDemande, Integer pkAccess) {

        DemandeBO demandeBo = getCheckDemarcheDemandeBO(pkDemande, false);

        LOGGER.info("Récupération de l'accès cible en base...");
        Optional<AccessBO> accessBoOp = accessRepository.findById(pkAccess);

        if (accessBoOp.isEmpty()) {
            throw new DemarchesServiceException("Accès cible introuvable", HttpStatus.NOT_FOUND);
        }

        LOGGER.info("Association de la demande...");

        demandeBo.setFkAccess(accessBoOp.get());
        demandeBo.setCanal(DemandeCanalEnum.GUICHET_VIRTUEL.name());

        demandeBo = demandesRepository.save(demandeBo);

        LOGGER.info("Association terminée");

        return demandesTransformer.bo2Dto(demandeBo);
    }

    @Override
    public boolean isAccesDesactive(Integer pkDemande) {
        DemandeBO demandeBo = getCheckDemarcheDemandeBO(pkDemande, false);
        return !demandeBo.getFkAccess().isActive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDTO changerAffectationDemande(int pkDemandes, String agentAffecteId) {
        try {
            DemandeBO demandeBo = getCheckDemarcheDemandeBO(pkDemandes, true);
            if (agentAffecteId != null) {
                if (demandeBo.getAgent() != null) {
                    demandeBo.getAgent().setId(Integer.valueOf(agentAffecteId));
                } else {
                    User user = utilisateursCache.get(agentAffecteId);
                    demandeBo.setAgent(demandesAgentsTransformer.user2Bo(user));
                }
            } else {
                demandeBo.setAgent(null);
            }

            demandesRepository.save(demandeBo);
            DemandeDTO demandeDTO = demandesTransformer.bo2Dto(demandeBo);

            LOGGER.info("Fin changement affectation...");
            return demandeDTO;
        } catch (Exception e) {
            LOGGER.error("Erreur lors de changerAffectationDemande");
            ErrorEventDTO esErrorEventDTO = transactionErrorsHandler.createErrorEvent(
                    "DemandesServiceImpl - méthode changerAffectationDemande()", pkDemandes, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDTO getDemande(String identifiant) {
        LOGGER.debug(RECUPERATION_DEMANDE);
        DemandeBO demandeBo = demandesRepository.findByIdentifiant(identifiant);
        return demandesTransformer.bo2Dto(demandeBo);
    }

    @Override
    public List<DemandeDTO> getAllDemandeForRelanceAvantPurge(Date dernierStatutDateDebut, Date dernierStatutDateFin,
            List<String> dernierStatutList) {

        LOGGER.info("Appel à DemandeService.getAllDemandeForRelanceAvantPurge");
        return demandesTransformer.bo2Dto(demandesRepository.findByDernierStatut_DateBetweenAndDernierStatut_NameIn(
                dernierStatutDateDebut, dernierStatutDateFin, dernierStatutList));

    }

    @Override
    public List<Integer> getAllDemandeIdsForPurge(Date dernierStatutDateDebut, List<String> dernierStatutList,
            List<String> canaux) {
        LOGGER.info("Appel à DemandeService.getAllDemandeIdsForPurge");
        return demandesRepository.findPkDemandesByDernierStatutDateBeforeAndDernierStatutNameInAndCanalIn(
                dernierStatutDateDebut, dernierStatutList, canaux);
    }

    @Override
    public AfDemandeExcelFlatIterable retrieveDemandesLazy(String plainStartDate, String plainEndDate, String statut) {
        Date startDate = null;
        Date endDate = null;

        SimpleDateFormat frenchDateFormat = new SimpleDateFormat(AfBackUtils.DEFAULT_FRENCH_DATE_FORMAT);
        if (StringUtils.isNotEmpty(plainStartDate)) {
            try {
                startDate = frenchDateFormat.parse(plainStartDate);
            } catch (ParseException e) {
                throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        if (StringUtils.isNotEmpty(plainEndDate)) {
            try {
                endDate = frenchDateFormat.parse(plainEndDate);
            } catch (ParseException e) {
                throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Last moment of days
            Calendar cal = Calendar.getInstance();
            cal.setTime(endDate);
            cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
            cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
            endDate = cal.getTime();
        }

        return new AfDemandeExcelFlatIterable(this, startDate, endDate, statut);
    }

}

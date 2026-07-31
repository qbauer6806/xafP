package mc.gouv.xaf.back.service.data.impl;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandesAgentsRepository;
import mc.gouv.xaf.back.data.dao.DemandesComplementsRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesUsagersRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.data.entity.DemandesAgentsBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsBO;
import mc.gouv.xaf.back.data.entity.DemandesUsagersBO;
import mc.gouv.xaf.back.data.model.ErrorEventDTO;
import mc.gouv.xaf.back.data.projection.DemandeExportDTO;
import mc.gouv.xaf.back.data.projection.DemandeLightProjection;
import mc.gouv.xaf.back.data.transformer.DemandesAgentsTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesUsagersTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.AfTemplateModelProvider;
import mc.gouv.xaf.back.service.DemandeFilesCategorizer;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.DemandesComplementsService;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.data.MarqueursService;
import mc.gouv.xaf.back.service.data.MotifsService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.demande.CloneDemandeExtender;
import mc.gouv.xaf.back.service.excel.AfDemandeExcelFlatIterable;
import mc.gouv.xaf.back.service.excel.AfExcelExportModelProvider;
import mc.gouv.xaf.back.service.handlers.TransactionErrorsHandler;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import mc.gouv.xaf.back.service.itg.nomen.PaysCache;
import mc.gouv.xaf.back.service.postprocessing.AfPostProcessingProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.back.service.utils.RechercheDemandesUtils;
import mc.gouv.xaf.back.service.utils.RelancesUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.AfDemandeExcelFlatDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import mc.gouv.xaf.shared.dto.DonneesMConnectDTO;
import mc.gouv.xaf.shared.dto.ExcelRechercheDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.MarqueurDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.PropertiesListEntityDTO;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import mc.gouv.xaf.shared.enums.DemandeComplementsStatutEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.velocity.tools.generic.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

/**
 * Service permettant la manipulation des demandes.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DemandesServiceImpl implements DemandesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesServiceImpl.class);
    private static final String RECUPERATION_DEMANDES = "Récupération en base des demandes...";
    private static final String RECUPERATION_DEMANDE = "Récupération en base de la demande...";

    private final DemandesRepository demandesRepository;
    private final DemandesAgentsRepository demandesAgentsRepository;
    private final DemandesUsagersRepository demandesUsagersRepository;
    private final AccessService accessService;
    private final AfPostProcessingProvider afPostProcessingProvider;
    private final DemandesStatutsService demandesStatutsService;
    private final DemarchesService demarchesService;
    private final FileService fileService;
    private final DemandesFilesService demandesFilesService;
    private final DemandesConfigHelperService demandesConfigHelperService;
    private final DemandesComplementsService demandesComplementsService;
    private final DemandesDataService demandesDataService;
    private final DemandesTransformer demandesTransformer;
    private final DemandesAgentsTransformer demandesAgentsTransformer;
    private final UtilisateursCache utilisateursCache;
    private final PaysCache paysCache;
    private final GouvPropertiesResolver gouvPropertiesResolver;
    private final RechercheDemandesUtils rechercheDemandesUtils;
    private final DemandesComplementsRepository demandesComplementsRepository;
    private final AfExcelExportModelProvider excelExportModelProvider;
    private final AfTemplateModelProvider afTemplateModelProvider;
    private final MotifsService motifsService;
    private final DemandeFilesCategorizer demandeFilesCategorizer;
    private final DemarchesDataProvider demarchesDataProvider;
    private final MarqueursService marqueursService;
    private final TransactionErrorsHandler transactionErrorsHandler;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DemandesUsagersTransformer demandesUsagersTransformer;
    private final Optional<CloneDemandeExtender> cloneDemandExtenders;
    private final DemandesHelperService demandesHelperService;
    private final PropertiesService propertiesService;

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
            DemandeConfigBO config = demandesConfigHelperService.getLastConfig();
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
                if (!champ.get("type").asString().equals("tableau") && !champ.get("type").asString()
                        .equals("fichier")) {
                    JsonNode mapping = champ.get("mapping");
                    String path = champ.get("path").asString();
                    processContenuTrad(contenuTrad, mappings, mapping, champ, path);
                }
            }
        }
        // récupérer aussi les champs tableau
        List<JsonNode> tableauxNodes = new ArrayList<>();
        extractTableauNodes(config.get("recap"), tableauxNodes);
        for (JsonNode tableau : tableauxNodes) {
            String rootPath = tableau.get("path").asString();
            // on regarde si dans le contenu on a une array correspondant à ce path
            JsonNode array = AfBackUtils.getNodeFromPath(contenuTrad, rootPath);
            for (JsonNode champ : tableau.get("columns")) {
                JsonNode mapping = champ.get("mapping");
                // il faut itérer sur chaque contenu du tableau
                for (int i = 0; i < array.size(); i++) {
                    String path = rootPath + "." + i + "." + champ.get("path").asString();
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
            node.properties().forEach(entry -> extractTableauNodes(entry.getValue(), tableauNodes));
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
                        String enumKey = element.asString();
                        JsonNode enumFound = mappings.get(mapping.asString()).get("languages").get("fr").get("values")
                                .get(enumKey);
                        if (enumFound != null) {
                            // si on trouve l'enum, alors on récupère la valeur
                            enumValue = enumFound.asString();
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
                    String enumKey = enumKeyNode.asString();
                    JsonNode isDynamic = champ.get("isDynamic");
                    if (isDynamic != null && !isDynamic.asBoolean()) {
                        JsonNode enumFound = mappings.get(mapping.asString()).get("languages").get("fr").get("values")
                                .get(enumKey);
                        if (enumFound != null) {
                            // si on trouve l'enum, alors on récupère la valeur
                            enumValue = enumFound.asString();
                        } else {
                            // sinon cela veut dire que la traduction a déjà été effectuée du coup on peut réutiliser la valeur
                            enumValue = enumKey;
                        }
                    } else if (mapping.asString().equals("nationalites")) {
                        enumValue = StringUtils.isBlank(enumKey)
                                ? ""
                                : paysCache.get(enumKey) != null ? paysCache.get(enumKey).getNationalite() : enumKey;
                    } else if (mapping.asString().equals("pays")) {
                        enumValue = StringUtils.isBlank(enumKey)
                                ? ""
                                : paysCache.get(enumKey) != null ? paysCache.get(enumKey).getLibelle() : enumKey;
                    } else if (mapping.asString().startsWith("properties_")) {
                        String propertyKey = mapping.asString().replaceFirst("^properties_", "") + "_FR";
                        PropertiesDTO prop = propertiesService.getProperty(propertyKey);
                        if (prop != null) {
                            PropertiesListEntityDTO[] listProperties = AfBackUtils.parserPropertiesListJson(
                                    prop.getValue());
                            if (null == listProperties || listProperties.length == 0) {
                                LOGGER.warn("Impossible de transformer la valeur de la dem_property (key={}) en map",
                                        propertyKey);
                                enumValue = enumKey;
                            } else {
                                Optional<PropertiesListEntityDTO> matchingObject = Arrays.stream(listProperties)
                                        .filter(e -> e.getId().equals(enumKey)).findFirst();
                                enumValue = matchingObject.map(PropertiesListEntityDTO::getLabel).orElse(enumKey);
                            }
                        }
                    }
                    AfBackUtils.setNodeValue(contenuTrad, path, enumValue);
                }
            }
        } else if (champ.get("type").asString().equals("adresse")) {
            // le champ est de type adresse donc on doit remplacer le pays
            path += ".pays";
            JsonNode enumKeyNode = AfBackUtils.getNodeFromPath(contenuTrad, path);
            if (enumKeyNode != null && !enumKeyNode.isNull() && !enumKeyNode.isMissingNode()) {
                String enumKey = enumKeyNode.asString();
                String enumValue = StringUtils.isBlank(enumKey)
                        ? ""
                        : paysCache.get(enumKey) != null ? paysCache.get(enumKey).getLibelle() : enumKey;
                AfBackUtils.setNodeValue(contenuTrad, path, enumValue);
            }
        } else if (champ.get("type").asString().equals("date")) {
            JsonNode dateNode = AfBackUtils.getNodeFromPath(contenuTrad, path);
            if (dateNode != null && !dateNode.isNull()) {
                String date = dateNode.asString();
                // Si la date a un format d'affichage
                String format = "dd/MM/yyyy";
                JsonNode formatNode = champ.get("displayJavaFormat");
                if (formatNode != null && !formatNode.isNull()) {
                    format = formatNode.asString();
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

    @Override
    public Optional<DemandeDTO> getDerniereDemandePourDuplication(Integer usagerId, List<String> statuts,
            List<String> buildIds) {
        checkAccess(usagerId);
        return demandesRepository.findFirstByUsager_IdAndDernierStatut_NameInAndConfig_BuildIdInOrderByDateCreationDesc(
                usagerId, statuts, buildIds).map(demandesTransformer::bo2Dto);
    }

    @Override
    public Optional<DemandeDTO> getDerniereDemande() {
        return demandesRepository.findFirstByOrderByDateCreationDesc().map(demandesTransformer::bo2Dto);
    }

    @Override
    public List<DemandeDTO> getDemandesLight(Integer usagerId) {
        LOGGER.info(RECUPERATION_DEMANDES);
        checkAccess(usagerId);
        return demandesRepository.findByUsagerId(usagerId).stream().map(demandesTransformer::lightProjection2Dto)
                .toList();
    }

    private void checkAccess(Integer usagerId) {
        AccessBO accessBo = accessService.getAccessBO(usagerId, true);
        if (accessBo == null) {
            throw new DemarchesServiceException("Accès correspondant introuvable", HttpStatus.NOT_FOUND);
        }
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
    public Page<AfDemandeExcelFlatDTO> retrieveDemandesExcelPageable(Pageable pageable,
            ExcelRechercheDTO excelRechercheDTO, long total) {
        Page<DemandeExportDTO> demandesPage = rechercheDemandesUtils.getDemandesExcelPageable(excelRechercheDTO,
                pageable, total);

        return demandesPage.map(demande -> {
            DemandeDTO dto = demandesTransformer.exportProjection2Dto(demande);
            // pour des questions de performances et éviter l'effet n+1 sur le onetomany, on doit récupérer les data dans un second temps
            dto.setData(demandesDataService.getDemandeDatasProjection(demande.getPkDemandes()));
            // mapper les marqueurs
            List<MarqueurDTO> marqueurs = marqueursService.getMarqueurs(demande.getConfig().getBuildId());
            dto.setMarqueurs(demandesTransformer.buildMarqueurs(marqueurs, demande.getContenu()));
            // mapper les marqueurs
            dto.setMarqueursTrad(demandesTransformer.buildMarqueurs(marqueurs, demande.getContenuTrad()));
            dto.setComplements(demandesComplementsService.getDemandesComplements(demande.getPkDemandes())
                    .toArray(DemandeComplementsDTO[]::new));
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
                .map(demandesTransformer::lightProjection2Dto).toList();
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

    @Override
    public List<DemandeDTO> getAllDemandesFilteredByDateAndStatut(String statut, Date date1, Date date2) {
        List<DemandeBO> demandes = demandesRepository.findAllByDernierStatut_NameAndDernierStatutDateGreaterThanAndDernierStatutDateLessThan(statut,
                date1, date2);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return demandesTransformer.bo2Dto(demandes);
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
                demandeComplementsDTO.getQuestion().setCodeMotif(motifsService.getMotif(codeMotif, "fr").getLibelle());
            }
        }
        // transformer le motif
        String codeMotif = demande.getDernierStatut().getCodeMotif();
        if (codeMotif != null) {
            MotifDTO motif = motifsService.getMotif(codeMotif, "fr");
            demande.getDernierStatut().setCodeMotif(motif != null ? motif.getLibelle() : codeMotif);
        }
        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                InputStream in = this.getClass().getResourceAsStream("/pdfrecap/DemandeRecap.docx")) {

            IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Velocity);

            LOGGER.info("Création du contexte avec le modèle fourni par la démarche...");
            IContext context = report.createContext();
            for (Entry<String, Object> entry : afTemplateModelProvider.getGenericModelPdf(demande).entrySet()) {
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
                    MarqueurDTO marqueur = marqueursService.getMarqueur(demande.getConfigBuildId(),
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
                        gouvPropertiesResolver.getDemarcheId() + "/" + gouvPropertiesResolver.getContainerId()
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
        DemandeBO demandeBo = demandesHelperService.getCheckDemarcheDemandeBO(pkDemandes, true);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return demandesTransformer.bo2Dto(demandeBo);
    }

    @Override
    public DemandeDTO updateDemande(DemandeDTO demande, boolean partialUpdate) {
        try {
            DemandeBO demandeBo = demandesHelperService.getCheckDemarcheDemandeBO(demande.getPkDemandes(), true);

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
    public DemandeDTO cloneDemande(Integer pkDemande) {
        return cloneDemande(pkDemande, false, false);
    }

    @Override
    public DemandeDTO cloneDemande(Integer pkDemande, boolean conserverAgent, boolean copierFichierInternes) {
        try {
            DemandeBO originalDemandeBo = demandesHelperService.getCheckDemarcheDemandeBO(pkDemande, true);

            LOGGER.info("Duplication de la demande...");
            DemandeDTO demandeDto = demandesTransformer.bo2Dto(originalDemandeBo, true);
            DemandeBO clonedDemandeBo = fillAndSaveDemandeToClone(demandeDto, originalDemandeBo);

            // Pièces jointes des demandes
            demandesFilesService.clonerDesPiecesJointes(originalDemandeBo, clonedDemandeBo);

            // Demandes d'informations complémentaires des demandes
            demandesComplementsService.clonerDemandeComplements(originalDemandeBo, clonedDemandeBo);

            // Fichiers internes
            if (copierFichierInternes) {
                demandesFilesService.clonerDesFichiersInternes(originalDemandeBo, clonedDemandeBo);
            }

            // Data des demandes
            demandesDataService.clonerDemandeData(originalDemandeBo, clonedDemandeBo);

            if (conserverAgent) {
                changerAffectationDemande(clonedDemandeBo.getPkDemandes(), originalDemandeBo.getAgent().getId());
            }

            // si la demande dupliquée contient des fichiers purgés par l'agent, on les retire du contenu
            demandesFilesService.supprimerPieceJustificativeContenu(clonedDemandeBo);


            // Génération d'un nouvel identifiant de demande
            String identifiant = generatePublicID();
            clonedDemandeBo.setIdentifiant(identifiant);

            clonedDemandeBo = demandesRepository.save(clonedDemandeBo);

            final var finalClonedDemandeBo = clonedDemandeBo; // lambda requires final variable
            cloneDemandExtenders.ifPresent(
                    finalizer -> finalizer.applyCloneTreatment(originalDemandeBo, finalClonedDemandeBo));

            LOGGER.info("Duplication terminée");

            // On passe tous les demandes complements à repondue pour la demande dupliquée
            // cf #66472 - [INCIDENT] [BO] erreur 500 sur demande d'info comp sur une demande annulée et dupliquée
            for (DemandesComplementsBO compl : clonedDemandeBo.getDemandesComplements()) {
                compl.setStatut(DemandeComplementsStatutEnum.REPONDUE.name());
                demandesComplementsRepository.save(compl);
                LOGGER.info("Passage de l'info compl : {} à répondue car duplication de la demande {}",
                        compl.getPkDemandesComplements(), pkDemande);
            }
            // On supprime DATES_RELANCES_KEY si la demande d'origine a été annulée pendant une relance
            demandesDataService.deleteDemandeData(clonedDemandeBo.getPkDemandes(), RelancesUtils.DATES_RELANCES_KEY);

            return demandesTransformer.bo2Dto(clonedDemandeBo);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de cloneDemande");
            ErrorEventDTO esErrorEventDTO = transactionErrorsHandler.createErrorEvent(
                    "DemandesServiceImpl - méthode cloneDemande()", pkDemande, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private DemandeBO fillAndSaveDemandeToClone(DemandeDTO demandeDto, DemandeBO originalDemandeBO) {
        DemandeBO newDemandeBo = demandesTransformer.dto2Bo(demandeDto);
        newDemandeBo.setFkAccess(originalDemandeBO.getFkAccess());
        newDemandeBo.setPkDemandes(null);
        newDemandeBo.setRecapType(originalDemandeBO.getRecapType());
        newDemandeBo.setDonneesCertifiees(originalDemandeBO.getDonneesCertifiees());
        newDemandeBo.setConfig(originalDemandeBO.getConfig());
        newDemandeBo.setTypeConnexionUsager(originalDemandeBO.getTypeConnexionUsager());
        // #4840 Enlever l'affectation
        newDemandeBo.setAgent(null);
        newDemandeBo.setUsager(originalDemandeBO.getUsager());
        return demandesRepository.save(newDemandeBo);
    }

    @Override
    public PagedModel<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche, Pageable pageable,
            String[] fields) {

        Long totalCount = rechercheDemandesUtils.getDemandesCount(demandeRecherche);
        List<DemandeBO> demandes = rechercheDemandesUtils.getDemandesPageable(demandeRecherche, pageable);
        List<DemandeDTO> demandesDto = demandesTransformer.bo2Dto(demandes, fields);
        demandesDto.forEach(demandesTransformer::hideInfosPageable);

        Page<DemandeDTO> page = new PageImpl<>(demandesDto, pageable, totalCount);
        return new PagedModel<>(page);
    }

    @Override
    public mc.gouv.xaf.shared.dto.Page<DemandeDTO> getDemandesPageable(Integer usagerId, List<String> status,
            PageParamDTO paramDTO) {
        String sortColumn = paramDTO.getSort();
        Sort sort = "DESC".equals(paramDTO.getDirection()) ? Sort.by(sortColumn).descending() : Sort.by(sortColumn);
        Pageable pageable = PageRequest.of(paramDTO.getPage(), paramDTO.getSize(), sort);
        Page<DemandeBO> bos;
        if (status != null) {
            bos = demandesRepository.findByFkAccessUsagerIdAndFkAccessActiveTrueAndDernierStatutNameIn(usagerId, status,
                    pageable);
        } else {
            bos = demandesRepository.findByFkAccessUsagerIdAndFkAccessActiveTrue(usagerId, pageable);
        }
        return demandesTransformer.boPage2DtoPage(bos);
    }

    @Override
    public List<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche) {
        List<DemandeBO> demandes = rechercheDemandesUtils.getDemandes(demandeRecherche);
        return demandesTransformer.bo2Dto(demandes);
    }

    @Override
    public DemandeDTO associerDemandeCourrier(Integer pkDemande, GichuniUsagerDTO gichuniUsagerDTO) {

        DemandeBO demandeBo = demandesHelperService.getCheckDemarcheDemandeBO(pkDemande, false);

        Integer usagerId = gichuniUsagerDTO.getId();

        LOGGER.info("Appel à DEM pour récupération de l'accès actuel de l'usager à cette démarche...");
        AccessBO accessBo = accessService.getAccessBOActive(usagerId);

        LOGGER.info("Association de la demande...");

        demandeBo.setFkAccess(accessBo);
        demandeBo.setCanal(DemandeCanalEnum.GUICHET_VIRTUEL.name());
        // assigner le bon usagerId (celui du téléservice et pas celui du courrier)
        DemandesUsagersBO usagerBO = demandesUsagersRepository.findOneById(usagerId);
        if (usagerBO == null) {
            // si l'usager n'existe pas on le créé
            usagerBO = demandesUsagersTransformer.user2Bo(gichuniUsagerDTO);
        }
        demandeBo.setUsager(usagerBO);

        demandeBo = demandesRepository.save(demandeBo);

        LOGGER.info("Association terminée");

        return demandesTransformer.bo2Dto(demandeBo);
    }

    @Override
    public boolean isAccesDesactive(Integer pkDemande) {
        DemandeBO demandeBo = demandesHelperService.getCheckDemarcheDemandeBO(pkDemande, false);
        return !demandeBo.getFkAccess().isActive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDTO changerAffectationDemande(int pkDemandes, String agentAffecteId) {
        try {
            DemandeBO demandeBO = demandesHelperService.getCheckDemarcheDemandeBO(pkDemandes, true);

            if (agentAffecteId == null) {
                demandeBO.setAgent(null);
            } else {
                DemandesAgentsBO agentsBO = demandesAgentsRepository.findById(agentAffecteId).orElseGet(() -> {
                    User user = utilisateursCache.get(agentAffecteId);
                    return demandesAgentsRepository.save(demandesAgentsTransformer.user2Bo(user));
                });
                demandeBO.setAgent(agentsBO);
            }

            return demandesTransformer.bo2Dto(demandesRepository.save(demandeBO));
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
    public AfDemandeExcelFlatIterable retrieveDemandesExcel(ExcelRechercheDTO excelRecherche) {
        long total = rechercheDemandesUtils.countDemandesExcel(excelRecherche);
        return new AfDemandeExcelFlatIterable(this, excelRecherche, total);
    }

    @Override
    public List<DemandeDTO> getDemandesLightUsagerActive(Integer usagerId) {
        LOGGER.info("Récupération en base des demandes pour l'usager {}", usagerId);
        checkAccess(usagerId);
        return demandesRepository.findByFkAccessUsagerIdAndFkAccessActiveTrue(usagerId, DemandeLightProjection.class)
                .stream().map(demandesTransformer::lightProjection2Dto).toList();
    }

}

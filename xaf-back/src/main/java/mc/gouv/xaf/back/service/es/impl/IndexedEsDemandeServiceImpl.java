package mc.gouv.xaf.back.service.es.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import mc.gouv.file.apiclient.FileClient;
import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.RechercheChampConfigRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsBO;
import mc.gouv.xaf.back.data.entity.DemandesCourriersBO;
import mc.gouv.xaf.back.data.entity.RechercheChampConfigBO;
import mc.gouv.xaf.back.data.es.dao.DemandeEsRepository;
import mc.gouv.xaf.back.data.es.model.*;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesCourriersTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesFilesTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.exception.AfIndexingException;
import mc.gouv.xaf.back.exception.FileConnectionException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.impl.DemandesServiceImpl;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.es.handlers.EsTransactionErrorsHandler;
import mc.gouv.xaf.back.service.es.transformer.DemandeEsTransformer;
import mc.gouv.xaf.back.service.pdf.PdfTypeEnum;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.back.service.utils.ESQueryUtils;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.tika.exception.TikaException;
import org.apache.tika.exception.ZeroByteFileException;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filters;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator.KeyedFilter;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilters;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.glassfish.jersey.internal.guava.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.ElasticsearchException;
import org.springframework.data.elasticsearch.core.DefaultResultMapper;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ResultsMapper;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.join.query.JoinQueryBuilders.hasChildQuery;

/**
 * Service permettant de faire de la recherche full-text sur les demandes en utilisant le moteur elasticsearch
 *
 * @author asouabni.ext
 */
@Primary
@Service
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackOn = Exception.class)
public class IndexedEsDemandeServiceImpl extends DemandesServiceImpl implements IndexedDemandeService {

    public static final String ES_KEYWORD = ".keyword";
    public static final SimpleDateFormat SDF = new SimpleDateFormat(DATE_PATTERN);
    public static final String ES_MAPPING_PROPERTIES_KEY = "properties";
    public static final String ES_MAPPING_FIELDS_KEY = "fields";
    public static final String ES_MAPPING_TYPE_KEY = "type";
    public static final String FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX = "complement.";
    public static final String INTERNAL_FILE_HIGHLIGHT_AND_FACET_PREFIX = "fichierinterne.";
    public static final String COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX = "courrier.";
    public static final String FILE_PROPERTIES_PREFIX = "fichiers.";
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexedEsDemandeServiceImpl.class);
    private final List<EsProperty> demandesProperties = new ArrayList<>();
    private final List<EsProperty> filesProperties = new ArrayList<>();
    //Map contenant les champs et le boost (si on veut augmenter le score de la recherche par rapport à un champ)
    //sur lesquels on va faire la recherche du type demandes de l'index <application.name>-index
    private final Map<String, Float> demandesPropertiesWithBoost = new HashMap<>();
    //Map contenant les champs et le boost (si on veut augmenter le score de la recherche par rapport à un champ)
    //sur lesquels on va faire la recherche du type fichiers de l'index <application.name>-index
    private final Map<String, Float> filesPropertiesWithBoost = new HashMap<>();
    private final Map<String, String> propertiesFields = new HashMap<>();
    //Liste des champs à exclure de la recherche des demandes
    private final List<String> demandesFieldsToExclude = new ArrayList<>();
    //Liste des champs à exclure de la recherche dans les fichiers associés aux demandes
    private final List<String> fichiersFieldsToExclude = new ArrayList<>();
    @Autowired
    IndexedDemandeService demandesService;
    @Inject
    RechercheChampConfigRepository rechercheChampConfigRepository;
    @Inject
    DemarchesDataProvider demarchesDataProvider;
    List<EsProperty> allProperties = new ArrayList<>();
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Inject
    private DemandeEsRepository demandeEsRepository;
    @Inject
    private AccessService accessService;
    @Inject
    private DemandeEsTransformer demandeEsTransformer;
    @Inject
    private AfBackUtils afBackUtils;
    @Value("${application.name}")
    private String indexAlias;
    //Balise à insérer au début des mots recherchés dans le résultat de la recherche
    private String highlightPretags;
    //Balise à insérer à la fin des mots recherchés dans le résultat de la recherche
    private String highlightPosttags;
    @Inject
    private DemandesRepository demandesRepository;
    @Inject
    private ElasticsearchRestTemplate elasticsearchTemplate;
    @Inject
    private GouvPropertiesResolver gouvPropertiesResolver;
    private ResultsMapper resultsMapper;

    @PostConstruct
    public void init() {
        ElasticsearchConverter elasticsearchConverter = new MappingElasticsearchConverter(
                new SimpleElasticsearchMappingContext());
        resultsMapper = new DefaultResultMapper(elasticsearchConverter.getMappingContext());
        highlightPretags = gouvPropertiesResolver.getSearchHighlightPreTags();
        highlightPosttags = gouvPropertiesResolver.getSearchHighlightPostTags();
        loadProperties();
    }

    @Override
    public void loadProperties() {
    	reloadProperties();
        try {
            initMappingProperties(true);
        } catch (Exception e) {
            LOGGER.error(
                    "Erreur lors de l'initialisation du mapping elasticsarch: Vérifiez que elasticsearch est bien démarré");
            LOGGER.error(e.getMessage());
        }
    }

    private void reloadProperties() {
    	LOGGER.info("Chargement des propriétés de la recherche avancée et désactivation de celles à exclure du mappings elasticserach");
        List<RechercheChampConfigBO> propertiesToExclude = rechercheChampConfigRepository.findByEnabled(false);
        demandesFieldsToExclude.clear();
        fichiersFieldsToExclude.clear();
        if (propertiesToExclude != null) {
            for (RechercheChampConfigBO champConfigBo : propertiesToExclude) {
                if (champConfigBo.getCle().startsWith(FILE_PROPERTIES_PREFIX)
                        || champConfigBo.getCle().startsWith(FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX)
                        || champConfigBo.getCle().startsWith(INTERNAL_FILE_HIGHLIGHT_AND_FACET_PREFIX)
                        || champConfigBo.getCle().startsWith(COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX)
                ) {
                    fichiersFieldsToExclude.add(champConfigBo.getCle());
                } else {
                    demandesFieldsToExclude.add(champConfigBo.getCle());
                }
            }
        }
    }

    /**
     * Méthode permettant de supprimer tous les properties chargées
     */
    private void clearProperties() {
        demandesProperties.clear();
        filesProperties.clear();
        demandesPropertiesWithBoost.clear();
        filesPropertiesWithBoost.clear();
    }

    /**
     * Récupération des du mapping à partir d'un alias
     *
     * @param aliasName Nom de l'alias
     * @param type      Type de l'index
     * @return Mapping Elasticsearch
     */
    private Map getMapping(String aliasName, String type) {
        Assert.notNull(aliasName, "No index defined for putMapping()");
        Assert.notNull(type, "No type defined for putMapping()");
        Map mappings = null;
        try {
            GetIndexRequest request = new GetIndexRequest(indexAlias);
            GetIndexResponse response = elasticsearchTemplate.getClient().indices().get(request, DEFAULT);
            String[] indicesNames = response.getIndices();

            if (indicesNames == null || indicesNames.length == 0) {
                throw new AfIndexingException("Problem retrieving index name");
            }

            MappingMetaData indexMappings = response.getMappings().get(indicesNames[0]);
            mappings = indexMappings.getSourceAsMap();

        } catch (Exception e) {
            throw new ElasticsearchException("Error while getting mapping for indexName : " + aliasName + " type : "
                    + type + " " + e.getMessage());
        }
        return mappings;
    }

    /**
     * Méthode permettant d'initialiser les propriétés elasticsearch sur lesquelles on va faire la recherche
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public synchronized void initMappingProperties(boolean reload) {

        Map<String, Map> mapping = getMapping(indexAlias, DemandeEsDTO.INDEX_TYPE);

        if (reload) {
            clearProperties();
            // refs ##28082 - [BO] Problème résultat affichage d'une recherche avancée > Catégorie Autres
            // On reload les properties sinon dans une archi genTSA la map demandesFieldsToExclude et demandeFilesToExclude ne sont pas alignées sur les deux 
            // A moins de restart le BO (qui lui va call le loadProperty pour les deux noeuds)
            reloadProperties();
        }

        if (demandesProperties.isEmpty() || reload) {
            initMappingProperties(demandesProperties, mapping, demandesFieldsToExclude, false);
            initMappingPropertiesMap(demandesProperties, demandesPropertiesWithBoost);
        }

        if (filesProperties.isEmpty() || reload) {
            initMappingProperties(filesProperties, mapping, null, true);
            initMappingPropertiesMap(filesProperties, filesPropertiesWithBoost);
        }

        LOGGER.info("Fin de l'initMappingProperties");
    }

    /**
     * Méthode permettant d'initialiser une map des propriétés sur lesquelles on va faire la recherche avec le boost correspondant
     *
     * @param properties          Liste des propriétés
     * @param propertiesWithBoost Map avec le boost à initialiser
     */
    private void initMappingPropertiesMap(List<EsProperty> properties, Map<String, Float> propertiesWithBoost) {
        for (EsProperty prop : properties) {
            getSearchFields(prop.getName(), properties).forEach(p -> propertiesWithBoost.put(p, 1f));
        }
    }

    /**
     * Méthode permettant de parser le mapping elasticsearch pour avoir la liste des champs, leurs types et leurs sous fields
     *
     * @param properties      Liste des propriétés à remplir
     * @param mapping         Mapping récupéré à partir de l'API elasticsearch
     * @param fieldsToExclude Les champs qu'on veut pas récupérer
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private synchronized void initMappingProperties(List<EsProperty> properties, Map<String, Map> mapping,
                                                    List<String> fieldsToExclude, boolean isFilesDocs) {
        if (elasticsearchTemplate != null && mapping != null) {

            for (Entry<String, Map> entry : mapping.entrySet()) {

                if (entry.getKey().equals(ES_MAPPING_PROPERTIES_KEY)) {
                    Map<String, Map> map = entry.getValue();
                    for (Entry<String, Map> subMapentry : map.entrySet()) {
                        if ((subMapentry.getKey().equals(DemandeFileEsDTO.INDEX_FILES_JOIN_DOC) && isFilesDocs)
                                || (!isFilesDocs
                                && !subMapentry.getKey().equals(DemandeFileEsDTO.INDEX_FILES_JOIN_DOC))) {
                            properties.add(new EsProperty(subMapentry.getKey()));
                            getPropertyName(subMapentry.getValue(), subMapentry.getKey(), properties);
                        }
                    }
                } else {
                    Map<String, Map> mappingCheck = mapping.get(entry.getKey());
                    if (mappingCheck != null) {
                        mapping = mappingCheck;
                    }
                }

            }

            if (fieldsToExclude != null) {
                properties.removeIf(p -> fieldsToExclude.contains(p.getName()));
            }

        }
    }

    /**
     * Méthode permettant de parser une propriété à partir de son nom
     *
     * @param map          Map des propriétés
     * @param propertyName Nom de la propriété
     * @param properties   Liste des propriétés à remplir
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void getPropertyName(Map<String, Map> map, String propertyName, List<EsProperty> properties) {

        if (map == null || map.isEmpty()) {
            return;
        }
        for (Entry<String, Map> entry : map.entrySet()) {
            if (entry.getKey().equals(ES_MAPPING_PROPERTIES_KEY)) {
                Map<String, Map> submap = entry.getValue();
                for (Entry<String, Map> subMapentry : submap.entrySet()) {
                    String newFiledName = propertyName + "." + subMapentry.getKey();
                    Integer fieldIndex = properties.indexOf(new EsProperty(propertyName));
                    if (fieldIndex < 0) {
                        properties.add(new EsProperty(newFiledName));
                    } else {
                        properties.set(fieldIndex, new EsProperty(newFiledName));
                    }
                    getPropertyName(subMapentry.getValue(), newFiledName, properties);
                }
            } else if (entry.getKey().equals(ES_MAPPING_FIELDS_KEY)) {
                Map<String, Map> submap = entry.getValue();
                for (Entry<String, Map> subMapentry : submap.entrySet()) {

                    Integer fieldIndex = properties.indexOf(new EsProperty(propertyName));
                    if (fieldIndex >= 0) {
                        properties.get(fieldIndex).addField(subMapentry.getKey());
                        propertiesFields.put(propertyName + "." + subMapentry.getKey(), propertyName);
                    }
                }

            } else if (entry.getKey().equals(ES_MAPPING_TYPE_KEY)) {
                String type = (String) ((Object) entry.getValue());
                Integer fieldIndex = properties.indexOf(new EsProperty(propertyName));
                if (fieldIndex >= 0) {
                    //On exclut les champs de type boolean car il faussent la recherche
                    if (!type.equals(EsProperty.BOOLEAN_TYPE)) {
                        properties.get(fieldIndex).setType(type);
                    } else {
                        properties.remove(properties.get(fieldIndex));
                    }
                }

            }

        }

    }

    /**
     * Methode permettant de récupérer la liste des propriétés elasticsearch
     *
     * @return liste des propriétés elasticsearch
     */
    @Override
    public List<EsProperty> getProperties(boolean reload) {

        if (allProperties.isEmpty() || reload) {
            Map<String, Map> mapping = getMapping(indexAlias, DemandeEsDTO.INDEX_TYPE);
            initMappingProperties(allProperties, mapping, new ArrayList<>(), false);
            initMappingProperties(allProperties, mapping, new ArrayList<>(), true);
        }
        return allProperties;
    }

    /**
     * Méthode permettant l'indexation des fichiers des demandes
     *
     * @param demandes Liste des demandes dont on va indexer les fichiers
     * @throws IOException
     */
    private void indexFiles(Page<DemandeBO> demandes) throws IOException {

        if (demandes != null) {
            List<DemandeFileEsDTO> files = new ArrayList<>();

            for (DemandeBO demande : demandes) {
                fillFilesList(files, demande);
            }

            indexFiles(files);
        }
    }

    /**
     * Méthode permettant l'indexation des fichiers d'une demande
     *
     * @param demande Liste des demandes dont on va indexer les fichiers
     * @throws IOException
     */
    @Override
    public void indexFiles(DemandeBO demande) throws IOException {

        if (demande != null) {
            List<DemandeFileEsDTO> files = new ArrayList<>();

            fillFilesList(files, demande);

            indexFiles(files);
        }
    }

    /**
     * Méthode permettant l'indexation des fichiers d'une demande
     *
     * @param demande Demande dont on va indexer les fichiers
     * @throws IOException
     */
    @Override
    public void indexFiles(DemandeDTO demande) throws IOException {

        if (demande != null) {
            List<DemandeFileEsDTO> files = new ArrayList<>();
            fillFilesList(files, demande);
            indexFiles(files);
        }
    }

    /**
     * Méthode permettant de récupérer la liste des pieces jointes, des complements et courriers au format elasticsearch
     *
     * @param files   Liste des fichiers à remplir
     * @param demande Demande concernée
     * @throws IOException
     */
    private void fillFilesList(List<DemandeFileEsDTO> files, DemandeBO demande) throws IOException {

        List<DemandeFileDTO> demFiles = DemandesFilesTransformer
                .bo2Dto(new ArrayList<>(demande.getFiles()));

        demFiles.addAll(recupererCourriersDemandeFromBO(demande.getCourriers()));

        Set<DemandesComplementsBO> demComplements = demande.getDemandesComplements();
        DemandeDTO demandeDTO = DemandesTransformer.bo2Dto(demande);

        if (demComplements != null) {
            for (DemandesComplementsBO demComplement : demComplements) {
                List<DemandeFileDTO> cfiles = DemandesComplementsFilesTransformer
                        .toDemandeFileDTO(demComplement.getFiles());
                if (cfiles != null && !cfiles.isEmpty()) {
                    files.addAll(getFileEsContent(demandeDTO, DemandeFileEsDTO.TYPE.COMPLEMENT, cfiles));
                }
            }
        }

        fillPjsAndFichiersInternesAndCourriers(demFiles, files, demandeDTO);

        // Récupération des courriers
        Set<DemandesCourriersBO> courrierBOs = demande.getCourriers();
        if (courrierBOs != null) {
            List<DemandeCourrierDTO> courriers = DemandesCourriersTransformer.bo2Dto(Lists.newArrayList(courrierBOs));
            fillCourriers(courriers, files, demandeDTO);
        }
    }

    /**
     * Méthode permettant de récupérer la liste des pieces jointes, des complements et courriers au format elasticsearch
     *
     * @param files   Liste des fichiers à remplir
     * @param demande Demande concernée
     * @throws IOException
     */
    private void fillFilesList(List<DemandeFileEsDTO> files, DemandeDTO demande) throws IOException {

        DemandeComplementsDTO[] demComplements = demande.getComplements();

        if (demComplements != null) {
            for (DemandeComplementsDTO demComplement : demComplements) {
                if (demComplement.getReponse() != null && demComplement.getReponse().getFichiers() != null) {
                    List<DemandeFileDTO> cfiles = DemandesComplementsFilesTransformer
                            .toDemandeFileDTO(Arrays.asList(demComplement.getReponse().getFichiers()));
                    if (cfiles != null && !cfiles.isEmpty()) {
                        files.addAll(getFileEsContent(demande, DemandeFileEsDTO.TYPE.COMPLEMENT, cfiles));
                    }
                }
            }
        }

        List<DemandeFileDTO> fichiers = new ArrayList<>();
        if (demande.getFichiers() != null) {
            fichiers.addAll(Arrays.asList(demande.getFichiers()));
        }

        if (demande.getCourriers() != null) {
            fichiers.addAll(recupererCourriersDemandeFromDTO(Arrays.asList(demande.getCourriers())));
        }

        fillPjsAndFichiersInternesAndCourriers(fichiers, files, demande);
        if (demande.getCourriers() != null) {
            fillCourriers(Arrays.asList(demande.getCourriers()), files, demande);
        }
    }

    /**
     * Méthode permettant transformer des DemandeCourrierDTO en DemandeFileDTO
     *
     * @param courriers courriers d'une demande
     * @return list des fichiers à ajouter
     */
    private List<DemandeFileDTO> recupererCourriersDemandeFromBO(Set<DemandesCourriersBO> courriers) {
        return recupererCourriersDemandeFromDTO(DemandesCourriersTransformer
                .bo2Dto(new ArrayList<>(courriers)));
    }

    /**
     * Méthode permettant transformer des DemandeCourrierDTO en DemandeFileDTO
     *
     * @param courriers courriers d'une demande
     * @return list des fichiers à ajouter
     */
    private List<DemandeFileDTO> recupererCourriersDemandeFromDTO(List<DemandeCourrierDTO> courriers) {
        List<DemandeFileDTO> fichiers = new ArrayList<>();
        // Conversion DemandeCourrierBO en DemandeFileDTO pour faciliter l'indexation
        if (courriers != null) {
            for (DemandeCourrierDTO courrier : courriers) {
                DemandeFileDTO file = new DemandeFileDTO();
                file.setMeta(courrier.getMeta());
                file.setName(courrier.getName());
                file.setUrl(courrier.getUrl());
                file.setDate(courrier.getDateCreation());
                fichiers.add(file);
            }
        }
        return fichiers;
    }

    /**
     * Méthode permettant de remplir la liste des pieces jointes et des fichiers internes àindexer dans elaticsearch
     *
     * @param demFiles   Liste des fichiers de la demande extraits de la base de données
     * @param files      Liste des fichiers à indexer dans elasticsearch
     * @param demandeDTO dto de la demande
     * @throws IOException Exception Input/Output
     */
    private void fillPjsAndFichiersInternesAndCourriers(List<DemandeFileDTO> demFiles, List<DemandeFileEsDTO> files,
                                                        DemandeDTO demandeDTO) throws IOException {
        if (demFiles != null) {
            for (DemandeFileDTO file : demFiles) {
                files.add(getFileEsContent(demandeDTO, getDemandeFileType(file), file));
            }
        }
    }

    /**
     * Méthode permettant de remplir la liste courriers à indexer dans elaticsearch
     *
     * @param courriers  Liste des courriers de la demande extraits de la base de données
     * @param files      Liste des fichiers à indexer dans elasticsearch
     * @param demandeDTO dto de la demande
     * @throws IOException Exception Input/Output
     */
    private void fillCourriers(List<DemandeCourrierDTO> courriers, List<DemandeFileEsDTO> files,
                               DemandeDTO demandeDTO) throws IOException {
        if (courriers != null) {
            for (DemandeCourrierDTO courrier : courriers) {
                files.add(getFileEsContent(demandeDTO, DemandeFileEsDTO.TYPE.COURRIER, courrier));
            }
        }
    }


    /**
     * Méthode permettant de récupérer le type du fichier associé à la demande en se basant sur ses metas
     *
     * @param file fichier dont on doit vérifier le type
     * @return Type du fichier
     */
    private DemandeFileEsDTO.TYPE getDemandeFileType(DemandeFileDTO file) {
        DemandeFileEsDTO.TYPE fileType;
        if (FileUtils.isFileCreatedByFront(file.getMeta())) {
            fileType = DemandeFileEsDTO.TYPE.PIECE_JOINTE;
        }
        if (FileUtils.isFileCreatedByBack(file.getMeta()) && file.getMeta().contains(PdfTypeEnum.COURRIER.name())) {
            fileType = DemandeFileEsDTO.TYPE.COURRIER;
        } else {
            fileType = DemandeFileEsDTO.TYPE.FICHIER_INTERNE;
        }
        return fileType;
    }

    @Override
    public Long reindex() throws IOException {

        LOGGER.info("Début de la réindexation GLOBALE");
        if (demandeEsRepository != null) {
            long demCount = demandesRepository.count();
            LOGGER.info("Nombre de demandes à réindexer : {}", demCount);
            demandeEsRepository.deleteAll();
            final int size = gouvPropertiesResolver.getEsReindexBulkSize();
            LOGGER.info("Bulk size : {}", size);
            int additionalPage = (demCount % size > 0) ? 1 : 0;

            indexBulkDeDemandes(demCount, size, additionalPage);
            indexBulkDeFichiers(demCount, size, additionalPage);

            LOGGER.info("Fin de la réindexation");
            return demCount;
        }
        LOGGER.info("Fin de la réindexation");
        return 0L;
    }

    @Override
    public Long reindexDemandes() throws IOException {

        LOGGER.info("Début de la réindexation des DEMANDES");
        if (demandeEsRepository != null) {
            long demCount = demandesRepository.count();
            LOGGER.info("Nombre de demandes à réindexer : {}", demCount);
            Page<DemandeBO> demandes = demandesRepository.findAll(PageRequest.of(0, (int) demCount));
            List<DemandeEsDTO> demandesEs = demandeEsTransformer.toEs(demandes).toList();
            demandeEsRepository.deleteAll(demandesEs);
            final int size = gouvPropertiesResolver.getEsReindexBulkSize();
            LOGGER.info("Bulk size : {}", size);
            int additionalPage = (demCount % size > 0) ? 1 : 0;
            indexBulkDeDemandes(demCount, size, additionalPage);
            elasticsearchTemplate.refresh(DemandeEsDTO.class);

            LOGGER.info("Fin de la réindexation des demandes");
            return demCount;
        }
        LOGGER.info("Fin de la réindexation des demandes");
        return 0L;
    }

    public List<List<String>> getDemandesDesynchro() {
        long demCount = demandesRepository.count();
        List<DemandeBO> demandesBdd = demandesRepository.findAll(PageRequest.of(0, (int) demCount)).toList();
        List<String> identifiantsDemandesBdd = demandesBdd.stream().map(DemandeBO::getIdentifiant).collect(Collectors.toList());

        List<DemandeEsDTO> demandesEs = this.findAllDemandesLazy();
        List<String> identifiantsDemandesEs = demandesEs.stream().filter(d -> d.getPkDemandes() != null).map(DemandeEsDTO::getIdentifiant).collect(Collectors.toList());

        // [0] Demandes présentes dans ES mais pas en BDD
        // [1] Demandes présentes en BDD mais pas dans ES
        List<List<String>> ret = new ArrayList<>();
        List<String> bddMaisPasES = new ArrayList(CollectionUtils.subtract(identifiantsDemandesBdd, identifiantsDemandesEs));
        List<String> esMaisPasBDD = new ArrayList(CollectionUtils.subtract(identifiantsDemandesEs,identifiantsDemandesBdd));

        ret.add(esMaisPasBDD);
        ret.add(bddMaisPasES);
        return ret;
    }

    public List<String> reindexDemandesDesynchro() throws Exception {
        List<String> demandesSync = new ArrayList<>();

        // [0] Demandes présentes dans ES mais pas en BDD
        // [1] Demandes présentes en BDD mais pas dans ES
        List<List<String>> demandesDesynchro = getDemandesDesynchro();

        // Récupération des demandes
        long demCount = demandesRepository.count();
        List<DemandeBO> demandesBdd = demandesRepository.findAll(PageRequest.of(0, (int) demCount)).toList();

        // Supression des demandes dans ES
        for (String idDemande : demandesDesynchro.get(0)) {
            demandeEsRepository.deleteById(idDemande);
            demandesSync.add(idDemande);
        }

        // Indexation des demandes en BDD mais pas ES
        List<DemandeBO> demandesBoASynchro = demandesBdd.stream().filter(d -> demandesDesynchro.get(1).contains(d.getIdentifiant())).collect(Collectors.toList());
        for(DemandeBO demandeBO : demandesBoASynchro) {
            DemandeDTO demandeDTO = DemandesTransformer.bo2Dto(demandeBO);
            indexDemande(demandeDTO);
            demandesSync.add(demandeDTO.getIdentifiant());
        }

        return demandesSync;
    }

    private void indexBulkDeDemandes(long demCount, int size, int additionalPage) throws IOException {
        for (int i = 0; i < demCount / size + additionalPage; i++) {

            Page<DemandeBO> demandes = demandesRepository.findAll(PageRequest.of(i, size));
            Page<DemandeEsDTO> demandesEs = demandeEsTransformer.toEs(demandes);

            if (!demandesEs.getContent().isEmpty()) {
                indexDemandes(demandesEs);
            }
        }
    }

    private void indexBulkDeFichiers(long demCount, int size, int additionalPage) throws IOException {
        for (int i = 0; i < demCount / size + additionalPage; i++) {

            Page<DemandeBO> demandes = demandesRepository.findAll(PageRequest.of(i, size));

            if (!demandes.getContent().isEmpty()) {
                indexFiles(demandes);
            }
            elasticsearchTemplate.refresh(DemandeEsDTO.class);
        }
    }

    @Override
    public void indexDemande(DemandeDTO demandeDTO) {
        Boolean activeAccess = accessService.isAccessActive(demandeDTO.getFkAccess());
        DemandeEsDTO demandeEsDTO = demandeEsTransformer.toEs(demandeDTO, activeAccess);
        try {
        	demandeEsRepository.save(demandeEsDTO);
		} catch (Exception e) {
	        LOGGER.error("Erreur d'indexation lors du clone de la demande.");
	        EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedEsDemandeServiceImpl - méthode cloneDemande()", demandeDTO, e);
	        applicationEventPublisher.publishEvent(esErrorEventDTO);
	        throw new AfIndexingException(e.getMessage(), e);
	    }
    }

    @Override
    public void indexElement(DemandeDTO demandeDTO, boolean indexFiles) throws IOException {

        if (demandeDTO != null) {

            Boolean activeAccess = accessService.isAccessActive(demandeDTO.getFkAccess());
            DemandeEsDTO demandeEsDTO = demandeEsTransformer.toEs(demandeDTO, activeAccess);

            List<DemandeFileEsDTO> files = null;
            if (indexFiles) {
                files = new ArrayList<>();
                fillFilesList(files, demandeDTO);
                files.addAll(files);
            }

            if (demandeEsDTO != null) {
                demandeEsRepository.save(demandeEsDTO);
            }
            if (files != null) {
                demandesService.indexFiles(files);
            }
        }

        LOGGER.info("Fin de l'indexation des fichiers");
    }

    @Override
    public void indexElement(DemandeFileDTO demandeFileDTO, DemandeDTO demandeDTO)
            throws IOException {

        if (demandeFileDTO != null) {

            DemandeFileEsDTO demandeFileEsDTO = getFileEsContent(demandeDTO, getDemandeFileType(demandeFileDTO), demandeFileDTO);
            List<DemandeFileEsDTO> demFileEsDtoList = new ArrayList<>();
            demFileEsDtoList.add(demandeFileEsDTO);

            LOGGER.info("Appel de la méthode indexFiles");
            demandesService.indexFiles(demFileEsDtoList);
        }

        LOGGER.info("Fin de l'indexation des fichiers");
    }

    @Override
    public void indexElement(DemandeFileDTO[] demandeFileDTOList, DemandeDTO demandeDTO)
            throws IOException {

        if (demandeFileDTOList != null) {

            List<DemandeFileEsDTO> demFileEsDtoList = new ArrayList<>();
            for (DemandeFileDTO file : demandeFileDTOList) {
                demFileEsDtoList.add(getFileEsContent(demandeDTO, getDemandeFileType(file), file));
            }

            LOGGER.info("Appel de la méthode indexFiles");
            demandesService.indexFiles(demFileEsDtoList);
        }

        LOGGER.info("Fin de l'indexation des fichiers");
    }

    /**
     * Méthode permettant de récupérer une demande de la base et de l'indexer
     *
     * @see mc.gouv.xaf.back.service.es.IndexedDemandeService#indexDemande(java.lang.String, java.lang.Integer)
     */
    @Override
    public void indexDemande(String demarcheId, Integer demandeId) {
        DemandeBO demandeBo = getDemandeBo(demarcheId, demandeId);
        DemandeDTO demandeDto = DemandesTransformer.bo2Dto(demandeBo);
        DemandeEsDTO demandeEsDTO = demandeEsTransformer.bo2Dto(demandeBo, null);
    	try {
    		demandeEsRepository.save(demandeEsDTO);
    	} catch (Exception e) {
	        LOGGER.error("Erreur d'indexation lors du clone de la demande.");
	        EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedEsDemandeServiceImpl - méthode indexDemande()", demandeDto, e);
	        applicationEventPublisher.publishEvent(esErrorEventDTO);
	        throw new AfIndexingException(e.getMessage(), e);
	    }

    }

    /**
     * Méthode permettant de récupérer une liste de DTO avec le contenu des fichier sous forme de chaine de caractéres
     * <br/>
     * les contenus des fichiers sont récupérés depuis le web service file
     *
     * @param demandeDTO      dto de la demande que nous voulons traiter
     * @param type            Type du fichier
     * @param demandeFileDTOs Liste des DTOs de fichiers à indexer
     * @return Liste des DTOs des fichiers indexés
     * @throws IOException
     */
    public List<DemandeFileEsDTO> getFileEsContent(DemandeDTO demandeDTO, DemandeFileEsDTO.TYPE type,
                                                   List<DemandeFileDTO> demandeFileDTOs) throws IOException {

        List<DemandeFileEsDTO> filesList = new ArrayList<>();

        if (demandeFileDTOs != null) {

            for (DemandeFileDTO demandeFileDTO : demandeFileDTOs) {
                filesList.add(getFileEsContent(demandeDTO, type, demandeFileDTO));
            }
        }
        return filesList;
    }

    /**
     * Méthode permettant de récupérer un DTO avec le contenu du fichier sous forme de chaine de caractéres <br/>
     * le contenu du fichier est récupéré depuis le web service file
     *
     * @param demande DTO de la demande ratachée au fichier
     * @param fichier DTO du fichier à indexé
     * @return Fichier indexé
     * @throws IOException
     */
    private DemandeFileEsDTO getFileEsContent(DemandeDTO demande, DemandeFileEsDTO.TYPE type,
                                              DemandeFileDTO fichier) throws IOException {

        if (fichier != null) {
            FileClient fileClient = new FileClient(gouvPropertiesResolver.getFileUrl(), gouvPropertiesResolver.getFileJwt());
            InputStream is;
            String fileUrl = "";
            try {
                String finalFilename = fichier.getUrl();
                String[] split = fichier.getUrl().split("/");
                String isolatedFileName = split[split.length - 1];
                finalFilename = finalFilename.replace(isolatedFileName, URLEncoder.encode(isolatedFileName, "UTF-8"));
                fileUrl = demande.getDemarcheId() + "/" + gouvPropertiesResolver.getContainerId() + "/" + finalFilename;
                LOGGER.info("Le fichier à indexer est le {}", fileUrl);
                is = fileClient.getFile(fileUrl);
            } catch (ConnectException e) {
                throw new FileConnectionException("Could not connect to file", e);
            }
            DemandeFileEsDTO demandeFileEsDTO = new DemandeFileEsDTO(demande.getIdentifiant());
            demandeFileEsDTO.getFichiers().setMeta(fichier.getMeta());
            demandeFileEsDTO.getFichiers().setName(fichier.getName());
            demandeFileEsDTO.getFichiers().setUrl(fichier.getUrl());
            demandeFileEsDTO.getFichiers().setType(type.name());
            demandeFileEsDTO.getFichiers().setIdentifiantDemande(demande.getIdentifiant());

            if (is != null) {
                String fileText = "";
                try {
                    fileText = FileUtils.parseToPlainText(is);
                    demandeFileEsDTO.getFichiers().setContent(fileText);
                    demandeFileEsDTO.getFichiers().setLanguage(demande.getLangue());

                } catch (ZeroByteFileException e) {
                    LOGGER.info("Le fichier : {} est vide (a une taille de 0 byte)", fileUrl);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            LOGGER.info("Parsing du fichier terminé");
            return demandeFileEsDTO;
        }
        return null;

    }

    /**
     * Méthode permettant de récupérer un DTO avec le contenu du fichier sous forme de chaine de caractéres <br/>
     * le contenu du fichier est récupéré depuis le web service file
     *
     * @param demande DTO de la demande ratachée au fichier
     * @param fichier DTO du fichier à indexé
     * @return Fichier indexé
     * @throws IOException
     */
    private DemandeFileEsDTO getFileEsContent(DemandeDTO demande, DemandeFileEsDTO.TYPE type,
                                              DemandeCourrierDTO fichier) throws IOException {

        if (fichier != null) {
            FileClient fileClient = new FileClient(gouvPropertiesResolver.getFileUrl(), gouvPropertiesResolver.getFileJwt());
            InputStream is;
            String fileUrl = "";
            try {
                String finalFilename = fichier.getUrl();
                String[] split = fichier.getUrl().split("/");
                String isolatedFileName = split[split.length - 1];
                finalFilename = finalFilename.replace(isolatedFileName, URLEncoder.encode(isolatedFileName, "UTF-8"));
                fileUrl = demande.getDemarcheId() + "/" + gouvPropertiesResolver.getContainerId() + "/" + finalFilename;
                LOGGER.info("Le fichier à indexer est le {}", fileUrl);
                is = fileClient.getFile(fileUrl);
            } catch (ConnectException e) {
                throw new FileConnectionException("Could not connect to file", e);
            }
            DemandeFileEsDTO demandeFileEsDTO = new DemandeFileEsDTO(demande.getIdentifiant());
            demandeFileEsDTO.getFichiers().setMeta(fichier.getMeta());
            demandeFileEsDTO.getFichiers().setName(fichier.getName());
            demandeFileEsDTO.getFichiers().setUrl(fichier.getUrl());
            demandeFileEsDTO.getFichiers().setType(type.name());
            demandeFileEsDTO.getFichiers().setIdentifiantDemande(demande.getIdentifiant());
            demandeFileEsDTO.getFichiers().setIdentifiant(fichier.getIdentifiant());
            demandeFileEsDTO.getFichiers().setPkDemandeFile(fichier.getPkCourrier());
            demandeFileEsDTO.getFichiers().setDateCreation(fichier.getDateCreation());
            demandeFileEsDTO.getFichiers().setPkDemande(demande.getPkDemandes());
            demandeFileEsDTO.getFichiers().setStatut(fichier.getFkStatut().getLibelle());
            demandeFileEsDTO.getFichiers().setDatePrinted(fichier.getDatePrinted());

            if (is != null) {
                String fileText = "";
                try {
                    fileText = FileUtils.parseToPlainText(is);
                    demandeFileEsDTO.getFichiers().setContent(fileText);
                    demandeFileEsDTO.getFichiers().setLanguage(demande.getLangue());

                } catch (ZeroByteFileException e) {
                    LOGGER.info("Le fichier : {} est vide (a une taille de 0 byte)", fileUrl);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            LOGGER.info("Parsing du fichier terminé");
            return demandeFileEsDTO;
        }
        return null;

    }

    /**
     * Méthode permettant d'indexer les demandes
     *
     * @param demandeEsDTOs Page des demandes à indexer
     * @return La page des demandes indexées
     */
    private Page<DemandeEsDTO> indexDemandes(Page<DemandeEsDTO> demandeEsDTOs) {

        if (demandeEsDTOs != null) {
            List<IndexQuery> indexList = new ArrayList<>();
            for (DemandeEsDTO dem : demandeEsDTOs) {
                IndexQuery index = new IndexQuery();
                index.setId(dem.getIdentifiant());
                index.setObject(dem);
                indexList.add(index);

            }

            elasticsearchTemplate.bulkIndex(indexList);
        }
        return demandeEsDTOs;
    }

    /**
     * Méthode permettant d'indexer un fichier
     *
     * @param demandeFileEsDTO Fichier à indexer
     * @return Fichier indexé
     */
    private DemandeFileEsDTO indexFile(DemandeFileEsDTO demandeFileEsDTO) {

        if (demandeFileEsDTO != null) {
            IndexQuery index = new IndexQuery();
            index.setId(demandeFileEsDTO.getFichiers().getId());
            index.setObject(demandeFileEsDTO);
            index.setSource(demandeFileEsDTO.getDemandeJoinField().getParent());
            elasticsearchTemplate.index(index);
        }
        return demandeFileEsDTO;
    }

    /**
     * Méthode permettant d'indexer une liste de fichiers
     *
     * @param demandeFileEsDTOs Liste des fichiers à indexer
     * @return Liste des fichiers indexées
     */
    @Override
    public List<DemandeFileEsDTO> indexFiles(List<DemandeFileEsDTO> demandeFileEsDTOs) throws IOException {

        List<IndexQuery> indexList = new ArrayList<>();

        if (demandeFileEsDTOs != null) {
            for (DemandeFileEsDTO demFile : demandeFileEsDTOs) {
                IndexQuery index = new IndexQuery();
                index.setId(demFile.getFichiers().getId());
                index.setObject(demFile);
                index.setParentId(demFile.getDemandeJoinField().getParent());
                indexList.add(index);
            }

            if (!indexList.isEmpty()) {
                bulkIndex(indexList);
                elasticsearchTemplate.refresh(DemandeFileEsDTO.class);
            }

        }
        return demandeFileEsDTOs;
    }

    public void bulkIndex(List<IndexQuery> queries) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (IndexQuery query : queries) {
            bulkRequest.add(prepareIndex(query));
        }
        checkForBulkUpdateFailure(elasticsearchTemplate.getClient().bulk(bulkRequest, RequestOptions.DEFAULT));
    }

    private void checkForBulkUpdateFailure(BulkResponse bulkResponse) {
        if (bulkResponse.hasFailures()) {
            Map<String, String> failedDocuments = new HashMap<>();
            for (BulkItemResponse item : bulkResponse.getItems()) {
                if (item.isFailed())
                    failedDocuments.put(item.getId(), item.getFailureMessage());
            }
            throw new ElasticsearchException(
                    "Bulk indexing has failures. Use ElasticsearchException.getFailedDocuments() for detailed messages ["
                            + failedDocuments + "]",
                    failedDocuments);
        }
    }

    private IndexRequest prepareIndex(IndexQuery query) {
        try {

            IndexRequest indexRequest = null;

            if (query.getObject() != null) {
                // If we have a query id and a document id, do not ask ES to generate one.
                indexRequest = new IndexRequest(indexAlias).type(DemandeEsDTO.INDEX_TYPE).id(query.getId());
                indexRequest.source(resultsMapper.getEntityMapper().mapToString(query.getObject()),
                        Requests.INDEX_CONTENT_TYPE);
            } else {
                throw new ElasticsearchException(
                        "object or source is null, failed to index the document [id: " + query.getId() + "]");
            }

            indexRequest.routing(query.getParentId());

            return indexRequest;
        } catch (IOException e) {
            throw new ElasticsearchException("failed to index the document [id: " + query.getId() + "]", e);
        }
    }

    @Override
    public List<DemandeEsDTO> getIndexedDemandes(DemandeRechercheDTO demandeRecherche) {
        demandeRecherche.setTexte(ESQueryUtils.getFormatedQuery(demandeRecherche.getTexte(),
                afBackUtils.getDemarcheInfos().getIdentifiantPrefixe()));
        return Lists.newArrayList(demandeEsRepository.search(getQueryBuilder(demandeRecherche)));
    }

    @Override
    public DemandesFacets getDemandesFacets(DemandeRechercheDTO demandeRecherche) {

        demandeRecherche.setTexte(ESQueryUtils.getFormatedQuery(demandeRecherche.getTexte(),
                afBackUtils.getDemarcheInfos().getIdentifiantPrefixe()));
        initMappingProperties(true);

        if (!StringUtils.isBlank(demandeRecherche.getTexte())) {

            NativeSearchQueryBuilder nativeSearchQueryBuilder = getFacetsAggregationQuery(demandeRecherche);

            return elasticsearchTemplate.query(nativeSearchQueryBuilder.build(), (SearchResponse response) -> {

                DemandesFacets facets = new DemandesFacets();

                if (response.getAggregations().asList().isEmpty()) {
                    return null;
                }

                for (Aggregation agg : response.getAggregations().asList()) {
                    ParsedFilters filters = (ParsedFilters) agg;
                    for (Filters.Bucket bucket : filters.getBuckets()) {
                        if (bucket.getDocCount() > 0) {
                            facets.add(new DemandesFacet(bucket.getKeyAsString(), bucket.getDocCount()));
                        }

                    }

                }
                return facets;
            });
        }

        return new DemandesFacets();

    }

    /**
     * Méthode permettant de récupérer la requete qui construit les facets
     *
     * @param demandeRecherche Paramètres de la recherche
     * @return Query builder avec la requete de récupération des facets
     */
    private NativeSearchQueryBuilder getFacetsAggregationQuery(DemandeRechercheDTO demandeRecherche) {

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withIndices(indexAlias)
                .withQuery(getQueryBuilder(demandeRecherche));

        List<KeyedFilter> queryStringQueryBuilders = new ArrayList<>();
        updateFilters(queryStringQueryBuilders, demandeRecherche.getTexte(), demandesProperties, false);
        updateFilters(queryStringQueryBuilders, demandeRecherche.getTexte(), filesProperties, true);

        if (!queryStringQueryBuilders.isEmpty()) {
            KeyedFilter[] queryStringQueryBuildersArray = new KeyedFilter[queryStringQueryBuilders.size()];
            for (int i = 0; i < queryStringQueryBuilders.size(); i++) {
                queryStringQueryBuildersArray[i] = queryStringQueryBuilders.get(i);
            }
            nativeSearchQueryBuilder = nativeSearchQueryBuilder
                    .addAggregation(AggregationBuilders.filters("facets", queryStringQueryBuildersArray));
        }

        return nativeSearchQueryBuilder;
    }

    /**
     * Méthode permettant de mettre à jour les filtres de la requete qui permet de recupérer les facets
     *
     * @param queryStringQueryBuilders Tableau des filtres
     * @param text                     Texte de la barre de recherche
     * @param properties               Liste des propriétés du document (demande ou fichier)
     * @param searchInChild            Boolean permettant d'indiquer si on recheche dans une demande ou dans un fils de la demande (fichier)
     */
    private void updateFilters(List<KeyedFilter> queryStringQueryBuilders, String text, List<EsProperty> properties, boolean searchInChild) {
        for (EsProperty property : properties) {
            if (!property.getType().equals(EsProperty.BOOLEAN_TYPE)) {
                Map<String, Float> fields = new HashMap<>();
                fields.put(property.getName(), 1f);
                if (!property.getFields().isEmpty()) {
                    for (String field : property.getFields()) {
                        fields.put(property.getName() + "." + field, 1f);
                    }
                }
                SimpleQueryStringBuilder sqsb = getSimpleQueryStringBuilder(text, fields);
                if (searchInChild) {
                    // Ajout du filtre pour les piéces jointes
                    addFileFilters(queryStringQueryBuilders, sqsb, property.getName(), DemandeFileEsDTO.TYPE.PIECE_JOINTE.name());
                    // Ajout du filtre pour les complements de demandes
                    addFileFilters(queryStringQueryBuilders, sqsb, FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX + property.getName(), DemandeFileEsDTO.TYPE.COMPLEMENT.name());
                    // Ajout du filtre pour les fichiers internes
                    addFileFilters(queryStringQueryBuilders, sqsb, INTERNAL_FILE_HIGHLIGHT_AND_FACET_PREFIX + property.getName(), DemandeFileEsDTO.TYPE.FICHIER_INTERNE.name());
                    // Ajout du filtre pour les courriers
                    addFileFilters(queryStringQueryBuilders, sqsb, COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX + property.getName(), DemandeFileEsDTO.TYPE.COURRIER.name());
                } else {
                    queryStringQueryBuilders.add(new KeyedFilter(property.getName(), sqsb));
                }
            }
        }
    }

    private void addFileFilters(List<KeyedFilter> queryStringQueryBuilders, SimpleQueryStringBuilder sqsb, String propertyName, String propertyType) {
        if (!fichiersFieldsToExclude.contains(propertyName)) {
            TermQueryBuilder termQueryBuilder = termQuery(DemandeFileEsDTO.TYPE_FIELD, propertyType);
            BoolQueryBuilder boolQueryBuilder = boolQuery().must(sqsb).must(termQueryBuilder);
            HasChildQueryBuilder hasChildQueryBuilder = hasChildQuery(DemandeFileEsDTO.INDEX_FILES_JOIN_DOC, boolQueryBuilder, ScoreMode.Avg);
            queryStringQueryBuilders.add(new KeyedFilter(propertyName, hasChildQueryBuilder));
        }
    }

    /**
     * Méthode permettant de mettre à jour les filtres de la requete qui permet de recupérer les facets
     *
     * @param queryStringQueryBuilders Tableau des filtres
     * @param index                    Index à partir du quel la mise à jour du tableau des filtres commence
     * @param text                     Texte de la barre de recherche
     * @param searchInChild            Boolean permettant d'indiquer si on recheche dans une demande ou dans un fils de la demande (fichier)
     * @param properties               Liste des propriétés du document (demande ou fichier)
     * @return Dernier index de mise à jour du tableau des filtres
     */
    private int updateFilters(KeyedFilter[] queryStringQueryBuilders, int index, String text, boolean searchInChild,
                              List<EsProperty> properties) {
        for (EsProperty property : properties) {
            if (!property.getType().equals(EsProperty.BOOLEAN_TYPE)) {
                Map<String, Float> fields = new HashMap<>();

                fields.put(property.getName(), 1f);

                if (!property.getFields().isEmpty()) {

                    for (String field : property.getFields()) {
                        fields.put(property.getName() + "." + field, 1f);
                    }
                }

                if (searchInChild) {

                    //Ajout du filtre pour les piéces jointes
                    SimpleQueryStringBuilder sqsb = getSimpleQueryStringBuilder(text, fields);
                    TermQueryBuilder pjtqb = termQuery(DemandeFileEsDTO.TYPE_FIELD,
                            DemandeFileEsDTO.TYPE.PIECE_JOINTE.name());
                    BoolQueryBuilder pjbqb = boolQuery().must(sqsb).must(pjtqb);
                    HasChildQueryBuilder pjHasChildQueryBuilder = hasChildQuery(DemandeFileEsDTO.INDEX_FILES_JOIN_DOC,
                            pjbqb, ScoreMode.Avg);

                    queryStringQueryBuilders[index] = new KeyedFilter(property.getName(), pjHasChildQueryBuilder);

                    index++;

                    //Ajout du filtre pour les complements de demandes
                    TermQueryBuilder comptqb = termQuery(DemandeFileEsDTO.TYPE_FIELD,
                            DemandeFileEsDTO.TYPE.COMPLEMENT.name());

                    BoolQueryBuilder compbqb = boolQuery().must(sqsb).must(comptqb);

                    HasChildQueryBuilder compHasChildQueryBuilder = hasChildQuery(DemandeFileEsDTO.INDEX_FILES_JOIN_DOC,
                            compbqb, ScoreMode.Avg);

                    queryStringQueryBuilders[index] = new KeyedFilter(
                            FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX + property.getName(), compHasChildQueryBuilder);

                    index++;

                    //Ajout du filtre pour les fichiers internes
                    TermQueryBuilder internalFilestqb = termQuery(DemandeFileEsDTO.TYPE_FIELD,
                            DemandeFileEsDTO.TYPE.FICHIER_INTERNE.name());

                    BoolQueryBuilder internalFilesbqb = boolQuery().must(sqsb).must(internalFilestqb);

                    HasChildQueryBuilder internalFilesHasChildQueryBuilder = hasChildQuery(
                            DemandeFileEsDTO.INDEX_FILES_JOIN_DOC, internalFilesbqb, ScoreMode.Avg);

                    queryStringQueryBuilders[index] = new KeyedFilter(
                            INTERNAL_FILE_HIGHLIGHT_AND_FACET_PREFIX + property.getName(),
                            internalFilesHasChildQueryBuilder);

                    index++;

                    //Ajout du filtre pour les courriers
                    TermQueryBuilder courriersTqb = termQuery(DemandeFileEsDTO.TYPE_FIELD,
                            DemandeFileEsDTO.TYPE.COURRIER.name());

                    BoolQueryBuilder courriersBqb = boolQuery().must(sqsb).must(courriersTqb);

                    HasChildQueryBuilder courriersHasChildQueryBuilder = hasChildQuery(
                            DemandeFileEsDTO.INDEX_FILES_JOIN_DOC, courriersBqb, ScoreMode.Avg);

                    queryStringQueryBuilders[index] = new KeyedFilter(
                            COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX + property.getName(),
                            courriersHasChildQueryBuilder);

                } else {
                    queryStringQueryBuilders[index] = new KeyedFilter(property.getName(),
                            getSimpleQueryStringBuilder(text, fields));
                }

                index++;
            }

        }
        return index;
    }

    /**
     * Méthode permettant de construire le SimpleQueryStringBuilder  permettant de faire la requete de recherche sur tous les champs en paramètres
     *
     * @param text   Texte de la recherche
     * @param fields Les fields sur lesquels on va faire la recherche
     * @return Le SimpleQueryStringBuilder permettant de faire la requete de recherche sur tous les champs en paramètres
     */
    private SimpleQueryStringBuilder getSimpleQueryStringBuilder(String text, Map<String, Float> fields) {

        SimpleQueryStringBuilder simpleQueryStringBuilder = simpleQueryStringQuery(text).lenient(true);
        if (fields != null) {
            return simpleQueryStringBuilder.fields(fields);
        }

        return simpleQueryStringBuilder;
    }

    @Override
    public Page<DemandeEsRechercheDTO> getIndexedDemandes(DemandeRechercheDTO demandeRecherche, Pageable pageable,
                                                          String[] fields) {

        demandeRecherche.setTexte(ESQueryUtils.getFormatedQuery(demandeRecherche.getTexte(),
                afBackUtils.getDemarcheInfos().getIdentifiantPrefixe()));
        initMappingProperties(true);

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withIndices(indexAlias)
                .withQuery(getQueryBuilder(demandeRecherche)).withPageable(pageable);

        nativeSearchQueryBuilder = highlightQuery(demandeRecherche, nativeSearchQueryBuilder);
        if (fields != null && fields.length > 0) {
            SourceFilter sourceFilter = new FetchSourceFilter(fields, null);
            nativeSearchQueryBuilder.withSourceFilter(sourceFilter);
        }

        return elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), DemandeEsRechercheDTO.class,
                new SearchResultMapper() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                        List<DemandeEsRechercheDTO> demandesEsList = new ArrayList<>();
                        if (response.getHits().getHits().length <= 0) {
                            return new AggregatedPageImpl<>(new ArrayList<>());
                        }

                        for (SearchHit searchHit : response.getHits()) {

                            DefaultResultMapper resultMapper = new DefaultResultMapper();
                            DemandeEsRechercheDTO demandeEsRechercheDTO = resultMapper
                                    .mapEntity(searchHit.getSourceAsString(), DemandeEsRechercheDTO.class);

                            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                            Map<String, String> demEsHighlightFields = new HashMap<>();
                            updateHighLightedField(highlightFields, demEsHighlightFields, false, false, false);

                            Map<String, SearchHits> innerHits = searchHit.getInnerHits();

                            if (innerHits != null) {
                                for (Entry<String, SearchHits> searchHitsEntry : innerHits.entrySet()) {
                                    SearchHit[] searchHitsArray = searchHitsEntry.getValue().getHits();
                                    for (SearchHit searchInnerHit : searchHitsArray) {

                                        DocumentField typeField = searchInnerHit.field(DemandeFileEsDTO.TYPE_FIELD);
                                        String type = typeField.getValue();

                                        boolean isInternalFile = type.equals(DemandeFileEsDTO.TYPE.FICHIER_INTERNE.name());
                                        boolean isComplement = type.equals(DemandeFileEsDTO.TYPE.COMPLEMENT.name());
                                        boolean isCourrier = type.equals(DemandeFileEsDTO.TYPE.COURRIER.name());

                                        updateHighLightedField(searchInnerHit.getHighlightFields(),
                                                demEsHighlightFields, isInternalFile, isComplement, isCourrier);
                                    }
                                }
                            }

                            demandeEsRechercheDTO.setHighlightedField(demEsHighlightFields);
                            demandesEsList.add(demandeEsRechercheDTO);
                        }

                        return new AggregatedPageImpl<>((List<T>) demandesEsList, pageable,
                                response.getHits().getTotalHits());
                    }

                    @Override
                    public <T> T mapSearchHit(SearchHit searchHit, Class<T> type) {
                        return null;
                    }

                });

    }

    @Override
    public Page<DemandeFileEsRechercheDTO> getIndexedCourriers(DemandeCourrierRechercheDTO demandeRecherche, Pageable pageable,
                                                               String[] fields) {

        demandeRecherche.setTexte(ESQueryUtils.getFormatedQuery(demandeRecherche.getTexte(),
                afBackUtils.getDemarcheInfos().getIdentifiantPrefixe()));
        initMappingProperties(false);

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withIndices(indexAlias)
                .withQuery(getQueryBuilderForCourrier(demandeRecherche)).withPageable(pageable);

        nativeSearchQueryBuilder = highlightQuery(demandeRecherche, nativeSearchQueryBuilder);
        if (fields != null && fields.length > 0) {
            SourceFilter sourceFilter = new FetchSourceFilter(fields, null);
            nativeSearchQueryBuilder.withSourceFilter(sourceFilter);
        }

        return elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), DemandeFileEsRechercheDTO.class,
                new SearchResultMapper() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz,
                                                            Pageable pageable) {
                        List<DemandeFileEsRechercheDTO> demandesEsList = new ArrayList<>();
                        if (response.getHits().getHits().length <= 0) {
                            return new AggregatedPageImpl<>(new ArrayList<>());
                        }

                        for (SearchHit searchHit : response.getHits()) {

                            DefaultResultMapper resultMapper = new DefaultResultMapper();
                            DemandeFileEsRechercheDTO fichierJoinEsRechercheDTO = resultMapper
                                    .mapEntity(searchHit.getSourceAsString(), DemandeFileEsRechercheDTO.class);

                            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                            Map<String, String> demEsHighlightFields = new HashMap<>();
                            updateHighLightedField(highlightFields, demEsHighlightFields, false, false, false);

                            Map<String, SearchHits> innerHits = searchHit.getInnerHits();

                            if (innerHits != null) {
                                for (Map.Entry<String, SearchHits> searchHitsEntry : innerHits.entrySet()) {
                                    SearchHit[] searchHitsArray = searchHitsEntry.getValue().getHits();
                                    for (SearchHit searchInnerHit : searchHitsArray) {

                                        DocumentField typeField = searchInnerHit.field(DemandeFileEsDTO.TYPE_FIELD);
                                        String type = typeField.getValue();

                                        boolean isCourrier = type.equals(DemandeFileEsDTO.TYPE.COURRIER.name());

                                        updateHighLightedField(searchInnerHit.getHighlightFields(),
                                                demEsHighlightFields, false, false, isCourrier);
                                    }
                                }
                            }

                            fichierJoinEsRechercheDTO.setHighlightedField(demEsHighlightFields);
                            demandesEsList.add(fichierJoinEsRechercheDTO);

                        }

                        return new AggregatedPageImpl<>((List<T>) demandesEsList, pageable,
                                response.getHits().getTotalHits());
                    }

                    @Override
                    public <T> T mapSearchHit(SearchHit searchHit, Class<T> type) {
                        return null;
                    }

                });

    }

    /**
     * Méthode permettant de construire la map ayant comme clé le champ ou la recherche à été faite et comme valeur les
     * fragments contenant le résultat de recherche.<br/>
     * Les mots clés de la recherche sont entourés par des balises qui les mettent en évidence
     *
     * @param highlightFields      Map des conetant les fragments surlignés récupérée de la recherche elasticsearch
     * @param demEsHighlightFields Map Contenant les fragments avec les mots clés surlignés associés aux champs ou la recherche a été
     *                             effectutée
     * @param isInternalFile       Boolean pour indiquer si on recherche dans les champs d'un fichier de type Fichier interne
     * @param isComplement         Boolean pour indiquer si on recherche dans les champs d'un fichier de type complement
     */
    private void updateHighLightedField(Map<String, HighlightField> highlightFields,
                                        Map<String, String> demEsHighlightFields, boolean isInternalFile, boolean isComplement, boolean isCourrier) {
        for (Entry<String, HighlightField> entry : highlightFields.entrySet()) {
            Text[] fragments = entry.getValue().fragments();
            if (fragments != null && fragments.length > 0) {

                // Construction du nom du champs
                StringBuilder fragmentFieldBuilder = new StringBuilder(entry.getKey());
                if (propertiesFields.get(fragmentFieldBuilder.toString()) != null) {
                    fragmentFieldBuilder = new StringBuilder(propertiesFields.get(fragmentFieldBuilder.toString()));
                }
                if (isComplement) {
                    fragmentFieldBuilder.insert(0, FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX);
                } else if (isCourrier) {
                    fragmentFieldBuilder.insert(0, COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX);
                } else if (isInternalFile) {
                    fragmentFieldBuilder.insert(0, INTERNAL_FILE_HIGHLIGHT_AND_FACET_PREFIX);
                }
                String fragmentField = fragmentFieldBuilder.toString();
                if (fichiersFieldsToExclude.contains(fragmentField)) {
                    // On ne veut pas afficher ce champs, donc on continue la boucle for
                    continue;
                }

                final String fragmentEdge = "...";
                final String fragmentSeparation = fragmentEdge + "<br/>" + fragmentEdge;

                String fragmentsAsString = Arrays.stream(fragments).map(Objects::toString)
                        .collect(Collectors.joining(fragmentSeparation));
                StringBuilder fragmentsSB = new StringBuilder(fragmentsAsString);
                if (fragments.length > 1) {
                    fragmentsSB.insert(0, fragmentEdge).append(fragmentEdge);
                }

                demEsHighlightFields.put(fragmentField, fragmentsSB.toString().replace("'", "&quot;")
                        .replace("\"", "\\\"").replace(highlightPretags.replace("\"", "\\\""), highlightPretags));
            }
        }
    }

    /**
     * Méthode permettant d'initialiser la requete highlight qui identifie les termes recherchés dans le document
     * elasticsearch
     *
     * @param demandeRecherche         Paramètres de la recherche
     * @param nativeSearchQueryBuilder Query builder
     * @return Query builder avec la requete highlight
     */
    private NativeSearchQueryBuilder highlightQuery(DemandeRechercheDTO demandeRecherche,
                                                    NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        if (!StringUtils.isBlank(demandeRecherche.getTexte())) {

            if (demandeRecherche.getSearchFields() != null && demandeRecherche.getSearchFields().length > 0) {
                List<String> demandeSearchFields = getSearchFields(demandeRecherche.getSearchFields(),
                        demandesProperties);
                HighlightBuilder.Field[] fieldsForHighlight = new HighlightBuilder.Field[demandeSearchFields.size()];
                int i = 0;

                if (!demandeSearchFields.isEmpty()) {
                    for (String serachField : demandeSearchFields) {
                        fieldsForHighlight[i] = getHighlightField(new HighlightBuilder.Field(serachField),
                                demandeRecherche.getTexte());

                        i++;
                    }
                }

                return nativeSearchQueryBuilder.withHighlightFields(fieldsForHighlight);
            } else {
                return nativeSearchQueryBuilder.withHighlightFields(
                        getHighlightField(new HighlightBuilder.Field("*"), demandeRecherche.getTexte()));
            }

        }
        return nativeSearchQueryBuilder;
    }

    /**
     * Méthode permettant de mettre à jour le field à highlighter
     *
     * @param field      Field à highlighter
     * @param searchText Recherche à faire
     * @return Field mis à jour
     */
    private HighlightBuilder.Field getHighlightField(HighlightBuilder.Field field, String searchText) {
        return field.preTags(highlightPretags).postTags(highlightPosttags)
                .highlightQuery(getSimpleQueryStringBuilder(searchText, demandesPropertiesWithBoost));
    }

    /**
     * Méthode permettant de récupérer les fields sur lesquels on va faire la recherche à partir d'une propriété
     *
     * @param propertyName Nom de la propriétés
     * @param properties   liste des propriétés elasticsearch
     * @return Liste des fields sur lesquels on va faire la recherche
     */
    private List<String> getSearchFields(String propertyName, List<EsProperty> properties) {
        List<String> searchFields = new ArrayList<>();
        if (properties.contains(new EsProperty(propertyName))) {
            EsProperty property = properties.get(properties.indexOf(new EsProperty(propertyName)));
            searchFields.add(propertyName);
            property.getFields().stream().forEach(field -> searchFields.add(propertyName + "." + field));
        }

        return searchFields;
    }

    private List<String> getSearchFields(String[] searchedProperties, List<EsProperty> properties) {
        List<String> searchFields = new ArrayList<>();
        for (String propertyName : searchedProperties) {
            searchFields.addAll(this.getSearchFields(propertyName, properties));
        }
        return searchFields;
    }

    /**
     * Méthode permettant la construction de la requete elasticserach de récupération des courriers
     *
     * @param demandeRecherche Paramètres de la recherche
     * @return Requete elasticsearch pour récupérer les demandes
     */
    private BoolQueryBuilder getQueryBuilderForCourrier(DemandeCourrierRechercheDTO demandeRecherche) {

        BoolQueryBuilder boolQueryBuilder = boolQuery();
        TermQueryBuilder tqb = termQuery(DemandeFileEsDTO.TYPE_FIELD, DemandeFileEsDTO.TYPE.COURRIER.name());
        boolQueryBuilder.must(tqb);

        if (!StringUtils.isBlank(demandeRecherche.getTexte())) {
            SimpleQueryStringBuilder filesQueryStringQueryBuilder = getSimpleQueryStringBuilder(
                    demandeRecherche.getTexte(), null);

            boolQueryBuilder = getQueryWhereForCourriers(filesQueryStringQueryBuilder,
                    demandeRecherche, demandeRecherche.getSearchFields());
        }

        if (demandeRecherche.getImprime()) {
            boolQueryBuilder.must(QueryBuilders.existsQuery(DemandeFileEsDTO.DATE_PRINTED_FIELD));
        } else {
            boolQueryBuilder.mustNot(QueryBuilders.existsQuery(DemandeFileEsDTO.DATE_PRINTED_FIELD));
        }

        return getUiFilterQuery(boolQueryBuilder, demandeRecherche);
    }

    /**
     * Méthode permettant la construction de la requete elasticserach de récupération des demandes
     *
     * @param demandeRecherche Paramètres de la recherche
     * @return Requete elasticsearch pour récupérer les demandes
     */
    private BoolQueryBuilder getQueryBuilder(DemandeRechercheDTO demandeRecherche) {

        BoolQueryBuilder boolQueryBuilder = boolQuery();

        if (!StringUtils.isBlank(demandeRecherche.getTexte())) {

            SimpleQueryStringBuilder demandeQueryStringQueryBuilder = getSimpleQueryStringBuilder(
                    demandeRecherche.getTexte(), null);
            SimpleQueryStringBuilder filesQueryStringQueryBuilder = getSimpleQueryStringBuilder(
                    demandeRecherche.getTexte(), null);

            if (demandeRecherche.getSearchFields() != null && demandeRecherche.getSearchFields().length > 0) {
                boolQueryBuilder = getQueryWhereFacetClicked(demandeQueryStringQueryBuilder,
                        filesQueryStringQueryBuilder, demandeRecherche.getTexte(), demandeRecherche.getSearchFields(),
                        boolQueryBuilder);

            } else {
                boolQueryBuilder = getQueryWhereFacetNotClicked(demandeQueryStringQueryBuilder,
                        filesQueryStringQueryBuilder, demandeRecherche.getTexte(), boolQueryBuilder);
            }

        }

        return getUiFilterQuery(boolQueryBuilder, demandeRecherche);

    }

    /**
     * Méthode permattant la construction de la requete de recupération des demandes lorsque on n'a pas cliqué sur aucune
     * facet
     *
     * @param demandeQueryStringQueryBuilder Requete sur les attributs de la demande
     * @param filesQueryStringQueryBuilder   Requete sur les attributs des fichiers
     * @param rechercheText                  Texte de la barre de recherche
     * @param boolQueryBuilder               Requete globale qui combine les requetes sur les demandes et sur les fichiers
     * @return Requete globale qui combine les requetes sur les demandes et sur les fichiers
     */
    private BoolQueryBuilder getQueryWhereFacetNotClicked(SimpleQueryStringBuilder demandeQueryStringQueryBuilder,
                                                          SimpleQueryStringBuilder filesQueryStringQueryBuilder, String rechercheText,
                                                          BoolQueryBuilder boolQueryBuilder) {
        demandeQueryStringQueryBuilder = demandeQueryStringQueryBuilder.fields(demandesPropertiesWithBoost)
                .lenient(true);
        filesQueryStringQueryBuilder = filesQueryStringQueryBuilder.fields(filesPropertiesWithBoost);
        HighlightBuilder.Field field = new HighlightBuilder.Field("*").preTags(highlightPretags)
                .postTags(highlightPosttags).highlightQuery(simpleQueryStringQuery(rechercheText)
                        .defaultOperator(Operator.OR).fields(filesPropertiesWithBoost).lenient(true));
        HighlightBuilder hb = new HighlightBuilder().field(field);
        InnerHitBuilder ihb = new InnerHitBuilder().setHighlightBuilder(hb)
                .setStoredFieldNames(Arrays.asList(DemandeFileEsDTO.TYPE_FIELD));
        HasChildQueryBuilder hasChildQueryBuilder = hasChildQuery(DemandeFileEsDTO.INDEX_FILES_JOIN_DOC,
                filesQueryStringQueryBuilder, ScoreMode.Avg).innerHit(ihb);
        return boolQueryBuilder.minimumShouldMatch(1).should(demandeQueryStringQueryBuilder)
                .should(hasChildQueryBuilder);
    }

    private BoolQueryBuilder getQueryWhereForCourriers(SimpleQueryStringBuilder filesQueryStringQueryBuilder, DemandeCourrierRechercheDTO recherche, String[] searchFields) {
        BoolQueryBuilder boolQueryBuilder = boolQuery();

        TermQueryBuilder tqb = termQuery(DemandeFileEsDTO.TYPE_FIELD, DemandeFileEsDTO.TYPE.COURRIER.name());
        boolQueryBuilder.must(tqb);

        // Supression du suffixe par type de fichier
        List<String> replacedSearchFields = new ArrayList<>();
        for (String searchField : searchFields) {
            replacedSearchFields.add(searchField.replaceFirst(COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX, ""));
        }
        List<String> searchFilesFields = getSearchFields(replacedSearchFields.toArray(new String[0]), filesProperties);

        if (!searchFilesFields.isEmpty()) {

            Map<String, Float> filesFields = new HashMap<>();
            HighlightBuilder hb = new HighlightBuilder();
            for (String f : searchFilesFields) {
                filesFields.put(f, 1f);
                HighlightBuilder.Field field = new HighlightBuilder.Field(f).preTags(highlightPretags)
                        .postTags(highlightPosttags)
                        .highlightQuery(getSimpleQueryStringBuilder(recherche.getTexte(), filesPropertiesWithBoost));
                hb = hb.field(field);

            }
            filesQueryStringQueryBuilder = filesQueryStringQueryBuilder.fields(filesFields);
            BoolQueryBuilder bqb = boolQuery().must(filesQueryStringQueryBuilder).must(tqb);
            boolQueryBuilder = boolQueryBuilder.must(bqb);

        }

        return boolQueryBuilder;
    }

    /**
     * Méthode permattant la construction de la requete de recupération des demandes lorsque on a cliqué sur une facet
     *
     * @param demandeQueryStringQueryBuilder Requete sur les attributs de la demande
     * @param filesQueryStringQueryBuilder   Requete sur les attributs des fichiers
     * @param rechercheText                  Texte de la barre de recherche
     * @param searchFields                   facet sur lequel on a cliqué
     * @param boolQueryBuilder               Requete globale qui combine les requetes sur les demandes et sur les fichiers
     * @return Requete globale qui combine les requetes sur les demandes et sur les fichiers
     */
    private BoolQueryBuilder getQueryWhereFacetClicked(SimpleQueryStringBuilder demandeQueryStringQueryBuilder,
                                                       SimpleQueryStringBuilder filesQueryStringQueryBuilder, String rechercheText, String[] searchFields,
                                                       BoolQueryBuilder boolQueryBuilder) {
        TermQueryBuilder tqb = null;

        // Supression du suffixe par type de fichier
        List<String> replacedSearchFields = new ArrayList<>();
        for (String searchField : searchFields) {
            if (searchField != null) {
                String replacedSearchField = searchField;
                if (searchField.startsWith(FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX)) {
                    replacedSearchField = searchField.replaceFirst(FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX, "");
                    tqb = termQuery(DemandeFileEsDTO.TYPE_FIELD, DemandeFileEsDTO.TYPE.COMPLEMENT.name());
                    boolQueryBuilder.must(hasChildQuery(DemandeFileEsDTO.INDEX_FILES_JOIN_DOC, tqb, ScoreMode.Avg));
                } else if (searchField.startsWith(FILE_PROPERTIES_PREFIX)) {
                    tqb = termQuery(DemandeFileEsDTO.TYPE_FIELD, DemandeFileEsDTO.TYPE.PIECE_JOINTE.name());
                    boolQueryBuilder.must(hasChildQuery(DemandeFileEsDTO.INDEX_FILES_JOIN_DOC, tqb, ScoreMode.Avg));
                } else if (searchField.startsWith(INTERNAL_FILE_HIGHLIGHT_AND_FACET_PREFIX)) {
                    replacedSearchField = searchField.replaceFirst(INTERNAL_FILE_HIGHLIGHT_AND_FACET_PREFIX, "");
                    tqb = termQuery(DemandeFileEsDTO.TYPE_FIELD, DemandeFileEsDTO.TYPE.FICHIER_INTERNE.name());
                    boolQueryBuilder.must(hasChildQuery(DemandeFileEsDTO.INDEX_FILES_JOIN_DOC, tqb, ScoreMode.Avg));
                } else if (searchField.startsWith(COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX)) {
                    replacedSearchField = searchField.replaceFirst(COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX, "");
                    tqb = termQuery(DemandeFileEsDTO.TYPE_FIELD, DemandeFileEsDTO.TYPE.COURRIER.name());
                    boolQueryBuilder.must(hasChildQuery(DemandeFileEsDTO.INDEX_FILES_JOIN_DOC, tqb, ScoreMode.Avg));
                }
                replacedSearchFields.add(replacedSearchField);
            }
        }

        boolQueryBuilder = boolQueryBuilder.minimumShouldMatch(1);

        List<String> searchDemandeFields = getSearchFields(replacedSearchFields.toArray(new String[0]), demandesProperties);
        List<String> searchFilesFields = getSearchFields(replacedSearchFields.toArray(new String[0]), filesProperties);

        if (!searchDemandeFields.isEmpty()) {
            Map<String, Float> demandeFields = new HashMap<>();
            searchDemandeFields.forEach(f -> demandeFields.put(f, 1f));
            demandeQueryStringQueryBuilder = demandeQueryStringQueryBuilder.fields(demandeFields);
            boolQueryBuilder = boolQueryBuilder.should(demandeQueryStringQueryBuilder);
        }

        if (!searchFilesFields.isEmpty()) {
            Map<String, Float> filesFields = new HashMap<>();
            HighlightBuilder hb = new HighlightBuilder();
            for (String f : searchFilesFields) {
                filesFields.put(f, 1f);
                HighlightBuilder.Field field = new HighlightBuilder.Field(f).preTags(highlightPretags)
                        .postTags(highlightPosttags)
                        .highlightQuery(getSimpleQueryStringBuilder(rechercheText, filesPropertiesWithBoost));
                hb = hb.field(field);
            }
            InnerHitBuilder ihb = new InnerHitBuilder().setHighlightBuilder(hb)
                    .setStoredFieldNames(Arrays.asList(DemandeFileEsDTO.TYPE_FIELD));
            filesQueryStringQueryBuilder = filesQueryStringQueryBuilder.fields(filesFields);
            HasChildQueryBuilder hasChildQueryBuilder;
            if (tqb != null) {
                BoolQueryBuilder bqb = boolQuery().must(filesQueryStringQueryBuilder).must(tqb);
                hasChildQueryBuilder = hasChildQuery(DemandeFileEsDTO.INDEX_FILES_JOIN_DOC, bqb, ScoreMode.Avg)
                        .innerHit(ihb);
            } else {
                hasChildQueryBuilder = hasChildQuery(DemandeFileEsDTO.INDEX_FILES_JOIN_DOC,
                        filesQueryStringQueryBuilder, ScoreMode.Avg).innerHit(ihb);
            }

            boolQueryBuilder = boolQueryBuilder.should(hasChildQueryBuilder);

        }
        return boolQueryBuilder;
    }

    /**
     * Méthode permettant la construction de la requete elasticsearch à partir des filtres de la recherche avancée
     *
     * @param boolQueryBuilder Requete globale qui combine les requetes sur les demandes, sur les fichiers et sur les filtres
     *                         définis dans l'interface graphique
     * @param demandeRecherche DTO contenant les champs de la recherche (filtres+barre de recherche)
     * @return Requete globale qui combine les requetes sur les demandes, sur les fichiers et sur les filtres définits
     * dans l'interface graphique
     */
    private BoolQueryBuilder getUiFilterQuery(BoolQueryBuilder boolQueryBuilder, DemandeRechercheDTO demandeRecherche) {

        String statutKey = DemandeEsDTO.DERNIER_STATUT_FIELD_NAME + "." + DemandeStatutEsDTO.CODE_FIELD_NAME
                + ES_KEYWORD;

        if (demandeRecherche.getAucunStatut()) {
            boolQueryBuilder = boolQueryBuilder
                    .mustNot(termsQuery(statutKey, demarchesDataProvider.getStatusMap().keySet()))
                    .must(existsQuery(statutKey));
        } else if (demandeRecherche.getStatuts() != null) {
            if (StringUtils.isNotBlank(demandeRecherche.getStatutPublicOuInterne())) {

                TermsQueryBuilder statutsQ = QueryBuilders.termsQuery(statutKey, demandeRecherche.getStatuts());
                MatchQueryBuilder statutPublicOuInterneQ = QueryBuilders.matchQuery("statutPublicOuInterne", demandeRecherche.getStatutPublicOuInterne());
                BoolQueryBuilder shouldQ = QueryBuilders.boolQuery().should(statutsQ).should(statutPublicOuInterneQ);
                boolQueryBuilder = boolQueryBuilder.must(shouldQ);
            } else {
                boolQueryBuilder = boolQueryBuilder.must(termsQuery(statutKey, demandeRecherche.getStatuts()));
            }
        } else if (StringUtils.isNotBlank(demandeRecherche.getStatutPublicOuInterne())) {
            boolQueryBuilder = boolQueryBuilder.must(matchQuery("statutPublicOuInterne", demandeRecherche.getStatutPublicOuInterne()));
        }

        String canauxKey = DemandeEsDTO.CANAL_FIELD_NAME + "." + CanalEsDto.CANAL_CODE_FIELD_NAME + ES_KEYWORD;

        if (demandeRecherche.getAucunCanal()) {
            boolQueryBuilder = boolQueryBuilder.mustNot(termsQuery(canauxKey, Arrays.asList(DemandeCanalEnum.values())
                    .stream().map(DemandeCanalEnum::name).collect(Collectors.toList()))).must(existsQuery(canauxKey));
        } else if (demandeRecherche.getCanaux() != null) {
            boolQueryBuilder = boolQueryBuilder.must(termsQuery(canauxKey,
                    demandeRecherche.getCanaux().stream().map(DemandeCanalEnum::name).collect(Collectors.toList())));
        }

        if (DemarchesUtils.isFrontUser()) {
            boolQueryBuilder = boolQueryBuilder
                    .must(termQuery(DemandeEsDTO.ACCESS_FIELD_NAME + "." + DemandeAccessEsDTO.ACTIVE_FIELD_NAME, true));
        }

        if (demandeRecherche.isAucunResponsable()) {
            boolQueryBuilder = boolQueryBuilder
                    .mustNot(existsQuery(DemandeEsDTO.AGENT_FIELD_NAME + "." + AgentEsDTO.MATRICULE_FIELD_NAME + ES_KEYWORD));
        } else if (!StringUtils.isBlank(demandeRecherche.getAgentAffecteId())) {
            boolQueryBuilder = boolQueryBuilder
                    .must(termQuery(DemandeEsDTO.AGENT_FIELD_NAME + "." + AgentEsDTO.MATRICULE_FIELD_NAME + ES_KEYWORD,
                            demandeRecherche.getAgentAffecteId()));
        }

//        if (!StringUtils.isBlank(demandeRecherche.getStatutPublicOuInterne())) {
//            boolQueryBuilder = boolQueryBuilder
//                    .should(matchQuery("statutPublicOuInterne",
//                    		demandeRecherche.getStatutPublicOuInterne()));
//        }

        RangeQueryBuilder rangeQueryBuilder = rangeQuery(DemandeEsDTO.DATE_CREATION_FIELD_NAME).format(DATE_PATTERN);

        if (demandeRecherche.getCreationStartDate() != null) {

            rangeQueryBuilder = rangeQueryBuilder.gte(getFormatedDate(demandeRecherche.getCreationStartDate()));

        }

        if (demandeRecherche.getCreationEndDate() != null) {
            rangeQueryBuilder = rangeQueryBuilder.lte(getFormatedDate(demandeRecherche.getCreationEndDate()));
        }

        if (demandeRecherche.getCreationStartDate() != null || demandeRecherche.getCreationEndDate() != null) {
            boolQueryBuilder = boolQueryBuilder.must(rangeQueryBuilder);
        }

        if (!StringUtils.isBlank(demandeRecherche.getIdentifiant())) {
            boolQueryBuilder = boolQueryBuilder.must(
                    termQuery(DemandeEsDTO.IDENTIFIANT_FIELD_NAME + ES_KEYWORD, demandeRecherche.getIdentifiant()));
        }

        DataRechercheDTO dataRechercheDTO = demandeRecherche.getData();

        // Pour le moment nous faisons un OU sur les data pour remonter
        // Les demandes en cours de traitement ET sur un agent OU data.IS_EN_ATTENTE_TRAITEMENT=1
        // En attendant un vrai service de recherche ou on pourra définir les OU / ET via json body (comme ES par
        // exemple)

        boolean predicatAnd = false;

        if (dataRechercheDTO != null) {
            if (dataRechercheDTO.getOperand() != null
                    && dataRechercheDTO.getOperand().equals(DataRechercheDTO.DataRechercheOperand.AND)) {
                predicatAnd = true;
            }
            // Pour le moment en fait on n'en gère qu'un
            //

            // HACK pour avoir tout ceux qui n'ont pas de data IS_EN_ATTENTE_VALIDATION
            // data=IS_EN_ATTENTE_VALIDATION=null
            // C'est à dire ceux dont le statut est en attente de traitement mais qui n'ont pas de data c'est à dire qui
            // ne sont pas en attente de validation
            if (StringUtils.equalsIgnoreCase(dataRechercheDTO.getValue(), "null")) {

                ExistsQueryBuilder existQueryBuilder = existsQuery(
                        DemandeEsDTO.DATA_FIELD_NAME + "." + dataRechercheDTO.getKey() + ES_KEYWORD);
                if (predicatAnd) {
                    boolQueryBuilder = boolQueryBuilder.mustNot(existQueryBuilder);
                } else {

                    BoolQueryBuilder tmpQB = boolQuery();
                    tmpQB = tmpQB.should(tmpQB.mustNot(existQueryBuilder));
                    tmpQB = tmpQB.should(boolQueryBuilder);
                    boolQueryBuilder = tmpQB;

                }

            } else {
                if (predicatAnd) {
                    boolQueryBuilder = boolQueryBuilder
                            .must(termQuery(DemandeEsDTO.DATA_FIELD_NAME + "." + dataRechercheDTO.getKey() + ES_KEYWORD,
                                    dataRechercheDTO.getValue()));
                } else {
                    BoolQueryBuilder tmpQB = boolQuery();
                    tmpQB = tmpQB.should(boolQueryBuilder);
                    tmpQB = tmpQB.should(
                            termQuery(DemandeEsDTO.DATA_FIELD_NAME + "." + dataRechercheDTO.getKey() + ES_KEYWORD,
                                    dataRechercheDTO.getValue()));
                    boolQueryBuilder = tmpQB;
                }

            }

        }

        return boolQueryBuilder;

    }

    @Override
    public DemandeDTO saveDemande(DemandeDTO demande, String premierStatut) throws Exception {
        DemandeDTO demandeDto = super.saveDemande(demande, premierStatut);
        try {
            indexElement(demandeDto, true);
        } catch (Exception e) {
            LOGGER.error("Erreur d'indexation lors de la sauvegarde de la demande.");
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedEsDemandeServiceImpl - méthode saveDemande()", demandeDto, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
        return demandeDto;
    }

    /**
     * Méhode permettant de mettre à jour une demande et de la réindexer
     */
    @Override
    public DemandeDTO updateDemande(DemandeDTO demande, boolean partialUpdate) throws IOException, SAXException {
        DemandeDTO demandeDTO = super.updateDemande(demande, partialUpdate);
        try {
            indexDemande(demandeDTO);
        } catch (Exception e) {
            LOGGER.error("Erreur d'indexation lors de l'update de la demande.");
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedEsDemandeServiceImpl - méthode updateDemande()", demandeDTO, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
        return demandeDTO;
    }

    /**
     * Méthode permettant de supprimer une demande et de la supprimer de l'index elasticsearch
     *
     * @see mc.gouv.xaf.back.service.data.impl.DemandesServiceImpl#deleteDemande(java.lang.String, java.lang.Integer)
     */
    @Override
    public void deleteDemande(String demarcheId, Integer demandeId) throws JsonProcessingException {
        try {
            Optional<DemandeBO> demandeBoOp = demandesRepository.findById(demandeId);
            demandeBoOp.ifPresent(demandeBO -> demandeEsRepository.deleteById(demandeBO.getIdentifiant()));
            super.deleteDemande(demarcheId, demandeId);
        } catch (Exception e) {
            LOGGER.error("Erreur d'indexation lors de la suppression de la demande.");
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedEsDemandeServiceImpl - méthode deleteDemande()", demarcheId, demandeId, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
    }

    /**
     * Méthode permettant de cloner une demande et d'indexer la nouvelle demande
     *
     * @param demarcheId Identifiant de la démarche
     * @param pkDemande  Identifiant de la demande
     * @return retourne de DTO de la demande
     * @see mc.gouv.xaf.back.service.data.impl.DemandesServiceImpl#cloneDemande(java.lang.String, java.lang.Integer)
     */
    @Override
    public DemandeDTO cloneDemande(String demarcheId, Integer pkDemande) {
        DemandeDTO demandeDTO = super.cloneDemande(demarcheId, pkDemande);
        try {
            indexElement(demandeDTO, true);
        } catch (Exception e) {
            LOGGER.error("Erreur d'indexation lors du clone de la demande.");
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedEsDemandeServiceImpl - méthode cloneDemande()", demandeDTO, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
        return demandeDTO;
    }

    /**
     * Méthode permettant de formatter une date au format 'dd/MM/yyyy'
     *
     * @param date La date à formatter
     * @return la date formattée
     */
    private String getFormatedDate(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        return SDF.format(cal.getTime());
    }


    /**
     * Récupère uniquement l'identifiant et la pkDemandes de tous les documents de l'index ES
     * @return List des demandes en Lazy
     */
    private List<DemandeEsDTO> findAllDemandesLazy() {
        String[] includes = new String[]{"identifiant", "pkDemandes"};
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .withSourceFilter(new FetchSourceFilter(includes, null))
                .withPageable(PageRequest.of(0, (int) demandeEsRepository.count()))
                .build();

        return elasticsearchTemplate.queryForList(searchQuery, DemandeEsDTO.class);
    }
}

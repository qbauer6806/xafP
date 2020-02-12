package mc.gouv.xaf.back.service.es.impl;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.simpleQueryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.join.query.JoinQueryBuilders.hasChildQuery;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import mc.gouv.xaf.back.data.entity.*;
import mc.gouv.xaf.back.data.transformer.DemandesCourriersTransformer;
import mc.gouv.xaf.back.service.pdf.PdfTypeEnum;
import mc.gouv.xaf.shared.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.tika.exception.TikaException;
import org.apache.tika.exception.ZeroByteFileException;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator.KeyedFilter;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilters;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilters.InternalBucket;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.glassfish.jersey.internal.guava.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.ElasticsearchException;
import org.springframework.data.elasticsearch.core.DefaultResultMapper;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsMapper;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;

import mc.gouv.file.apiclient.FileClient;
import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.RechercheChampConfigRepository;
import mc.gouv.xaf.back.data.es.dao.DemandeEsRepository;
import mc.gouv.xaf.back.data.es.model.AgentEsDTO;
import mc.gouv.xaf.back.data.es.model.CanalEsDto;
import mc.gouv.xaf.back.data.es.model.DemandeAccessEsDTO;
import mc.gouv.xaf.back.data.es.model.DemandeEsDTO;
import mc.gouv.xaf.back.data.es.model.DemandeEsJmsDto;
import mc.gouv.xaf.back.data.es.model.DemandeEsRechercheDTO;
import mc.gouv.xaf.back.data.es.model.DemandeFileEsDTO;
import mc.gouv.xaf.back.data.es.model.DemandeStatutEsDTO;
import mc.gouv.xaf.back.data.es.model.DemandesFacet;
import mc.gouv.xaf.back.data.es.model.DemandesFacets;
import mc.gouv.xaf.back.data.es.model.EsProperty;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesFilesTransformer;
import mc.gouv.xaf.back.exception.AfIndexingException;
import mc.gouv.xaf.back.exception.FileConnectionException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.impl.DemandesServiceImpl;
import mc.gouv.xaf.back.service.es.DemandeJmsTopicSendService;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.es.transformer.DemandeEsTransformer;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.back.service.utils.ESQueryUtils;
import mc.gouv.xaf.back.service.utils.FileUtils;

/**
 * Service permettant de faire de la recherche full-text sur les demandes en utilisant le moteur elasticsearch
 * 
 * @author asouabni.ext
 *
 */
@Primary
@Service
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackOn = Exception.class)
public class IndexedEsDemandeServiceImpl extends DemandesServiceImpl implements IndexedDemandeService {

    @Inject
    DemandeJmsTopicSendService demandeJmsService;

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

    @Autowired
    IndexedDemandeService demandesService;

    //Balise à insérer au début des mots recherchés dans le résultat de la recherche
    private String highlightPretags;
    //Balise à insérer à la fin des mots recherchés dans le résultat de la recherche
    private String highlightPosttags;

    @Inject
    private DemandesRepository demandesRepository;

    @Inject
    private ElasticsearchTemplate elasticsearchTemplate;

    @Inject
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Inject
    RechercheChampConfigRepository rechercheChampConfigRepository;

    @Inject
    DemarchesDataProvider demarchesDataProvider;

    @Autowired
    private EntityManager entityManager;

    private List<EsProperty> demandesProperties = new ArrayList<>();
    private List<EsProperty> filesProperties = new ArrayList<>();
    List<EsProperty> allProperties = new ArrayList<>();
    //Map contenant les champs et le boost (si on veut augmenter le score de la recherche par rapport à un champ) 
    //sur lesquels on va faire la recherche du type demandes de l'index <application.name>-index
    private Map<String, Float> demandesPropertiesWithBoost = new HashMap<>();
    //Map contenant les champs et le boost (si on veut augmenter le score de la recherche par rapport à un champ) 
    //sur lesquels on va faire la recherche du type fichiers de l'index <application.name>-index
    private Map<String, Float> filesPropertiesWithBoost = new HashMap<>();

    private Map<String, String> propertiesFields = new HashMap<>();

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

    //Liste des champs à exclure de la recherche des demandes
    private List<String> demandesFieldsToExclude = new ArrayList<>();

    //Liste des champs à exclure de la recherche dans les fichiers associés aux demandes
    private List<String> fichiersFieldsToExclude = new ArrayList<>();

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

    /**
     * Méthode permettant de charger les propriétés à exclure lors de la recherche avancée et du mappings elasticserach
     */
    @Override
    public void loadProperties() {

        List<RechercheChampConfigBO> propertiesToExclude = rechercheChampConfigRepository.findByEnabled(false);
        demandesFieldsToExclude.clear();
        fichiersFieldsToExclude.clear();
        if (propertiesToExclude != null) {
            for (RechercheChampConfigBO champConfigBo : propertiesToExclude) {
                if (champConfigBo.getCle().startsWith(FILE_PROPERTIES_PREFIX)) {
                    fichiersFieldsToExclude.add(champConfigBo.getCle());
                } else {
                    demandesFieldsToExclude.add(champConfigBo.getCle());
                }
            }
        }

        try {
            initMappingProperties(true);
        } catch (Exception e) {
            LOGGER.error(
                    "Erreur lors de l'initialisation du mapping elasticsarch: Vérifiez que elasticsearch est bien démarré");
            LOGGER.error(e.getMessage());
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
     * @param type Type de l'index
     * @return Mapping Elasticsearch
     */
    private Map getMapping(String aliasName, String type) {
        Assert.notNull(aliasName, "No index defined for putMapping()");
        Assert.notNull(type, "No type defined for putMapping()");
        Map mappings = null;
        try {

            String[] indicesNames = elasticsearchTemplate.getClient().admin().indices()
                    .getIndex(new GetIndexRequest().indices(indexAlias)).actionGet().getIndices();

            if (indicesNames == null || indicesNames.length == 0) {
                throw new AfIndexingException("Problem retrieving index name");
            }

            mappings = elasticsearchTemplate.getClient().admin().indices()
                    .getMappings(new GetMappingsRequest().indices(aliasName).types(type)).actionGet().getMappings()
                    .get(indicesNames[0]).get(type).getSourceAsMap();

        } catch (Exception e) {
            throw new ElasticsearchException("Error while getting mapping for indexName : " + aliasName + " type : "
                    + type + " " + e.getMessage());
        }
        return mappings;
    }

    /**
     * Méthode permettant d'initialiser les propriétés elasticsearch sur lesquelles on va faire la recherche
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public synchronized void initMappingProperties(boolean reload) {

        Map<String, Map> mapping = getMapping(indexAlias, DemandeEsDTO.INDEX_TYPE);

        if (reload) {
            clearProperties();
        }

        if (demandesProperties.isEmpty() || reload) {
            initMappingProperties(demandesProperties, mapping, demandesFieldsToExclude, false);
            initMappingPropertiesMap(demandesProperties, demandesPropertiesWithBoost);

        }

        if (filesProperties.isEmpty() || reload) {
            initMappingProperties(filesProperties, mapping, fichiersFieldsToExclude, true);
            initMappingPropertiesMap(filesProperties, filesPropertiesWithBoost);
        }
    }

    /**
     * Méthode permettant d'initialiser une map des propriétés sur lesquelles on va faire la recherche avec le boost correspondant
     * 
     * @param properties Liste des propriétés
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
     * @param properties Liste des propriétés à remplir
     * @param mapping Mapping récupéré à partir de l'API elasticsearch
     * @param fieldsToExclude Les champs qu'on veut pas récupérer
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
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
                    if (mappingCheck != null)
                        mapping = mappingCheck;
                    else
                        continue;
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
     * @param map Map des propriétés
     * @param propertyName Nom de la propriété
     * @param properties Liste des propriétés à remplir
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
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
     * @param demandes
     *            Liste des demandes dont on va indexer les fichiers
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
     * @param demande
     *            Liste des demandes dont on va indexer les fichiers
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
     * @param demande
     *            Demande dont on va indexer les fichiers
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
     * @param files
     *            Liste des fichiers à remplir
     * @param demande
     *            Demande concernée
     * @throws IOException
     */
    private void fillFilesList(List<DemandeFileEsDTO> files, DemandeBO demande) throws IOException {

        List<DemandeFileDTO> demFiles = DemandesFilesTransformer
                .bo2Dto(new ArrayList<>(demande.getFiles()));

        demFiles.addAll(recupererCourriersDemandeFromBO(demande.getCourriers()));

        Set<DemandesComplementsBO> demComplements = demande.getDemandesComplements();

        if (demComplements != null) {
            for (DemandesComplementsBO demComplement : demComplements) {
                List<DemandeFileDTO> cfiles = DemandesComplementsFilesTransformer
                        .toDemandeFileDTO(demComplement.getFiles());
                if (cfiles != null && !cfiles.isEmpty()) {
                    files.addAll(getFileEsContent(demande.getFkAccess().getDemarcheId(), demande.getIdentifiant(),
                            DemandeFileEsDTO.TYPE.COMPLEMENT, cfiles));
                }
            }
        }

        fillPjsAndFichiersInternesAndCourriers(demFiles, files, demande.getFkAccess().getDemarcheId(), demande.getIdentifiant());

    }

    /**
     * Méthode permettant de récupérer la liste des pieces jointes, des complements et courriers au format elasticsearch
     * 
     * @param files
     *            Liste des fichiers à remplir
     * @param demande
     *            Demande concernée
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
                        files.addAll(getFileEsContent(demande.getDemarcheId(), demande.getIdentifiant(),
                                DemandeFileEsDTO.TYPE.COMPLEMENT, cfiles));
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

        fillPjsAndFichiersInternesAndCourriers(fichiers, files, demande.getDemarcheId(), demande.getIdentifiant());

    }

    /**
     * Méthode permettant transformer des DemandeCourrierDTO en DemandeFileDTO
     * @param courriers courriers d'une demande
     * @return list des fichiers à ajouter
     */
    private List<DemandeFileDTO> recupererCourriersDemandeFromBO(Set<DemandesCourriersBO> courriers) {
        return recupererCourriersDemandeFromDTO(DemandesCourriersTransformer
                .bo2Dto(new ArrayList<>(courriers)));
    }

    /**
     * Méthode permettant transformer des DemandeCourrierDTO en DemandeFileDTO
     * @param courriers courriers d'une demande
     * @return list des fichiers à ajouter
     */
    private List<DemandeFileDTO> recupererCourriersDemandeFromDTO(List<DemandeCourrierDTO> courriers) {
        List<DemandeFileDTO> fichiers = new ArrayList<>();
        // Conversion DemandeCourrierBO en DemandeFileDTO pour faciliter l'indexation
        if (courriers != null) {
            for(DemandeCourrierDTO courrier: courriers) {
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
     * @param demFiles Liste des fichiers de la demande extraits de la base de données
     * @param files Liste des fichiers à indexer dans elasticsearch
     * @param demarcheId Identifiant de la démarche
     * @param demIdentifiant Identifiant de la demande
     * @throws IOException Exception Input/Output
     */
    private void fillPjsAndFichiersInternesAndCourriers(List<DemandeFileDTO> demFiles, List<DemandeFileEsDTO> files,
            String demarcheId, String demIdentifiant) throws IOException {
        if (demFiles != null) {
            for (DemandeFileDTO file : demFiles) {
                files.add(getFileEsContent(demarcheId, demIdentifiant, getDemandeFileType(file), file));
            }
        }
    }

    /**
     * Méthode permettant de récupérer le type du fichier associé à la demande en se basant sur ses metas
     * 
     * @param file fichier dont on doit vérifier le type
     * 
     * @return Type du fichier
     */
    private DemandeFileEsDTO.TYPE getDemandeFileType(DemandeFileDTO file) {
        DemandeFileEsDTO.TYPE fileType;
        if (FileUtils.isFileCreatedByFront(file.getMeta())) {
            fileType = DemandeFileEsDTO.TYPE.PIECE_JOINTE;
        } if (FileUtils.isFileCreatedByBack(file.getMeta()) && file.getMeta().contains(PdfTypeEnum.COURRIER.name())) {
            fileType = DemandeFileEsDTO.TYPE.COURRIER;
        } else {
        	fileType = DemandeFileEsDTO.TYPE.FICHIER_INTERNE;
        }
        return fileType;
    }

    @Override
    public Long reindex() throws IOException, SAXException, TikaException {

        LOGGER.info("Début de la réindexation");
        if (demandeEsRepository != null) {
            long demCount = demandesRepository.count();
            LOGGER.info("Nombre de demandes à réindexer : {}", demCount);
            demandeEsRepository.deleteAll();
            final int size = gouvPropertiesResolver.getEsReindexBulkSize();
            LOGGER.info("Bulk size : {}", size);
            int additionalPage = 0;
            if (demCount % size > 0) {
                additionalPage = 1;
            }
            for (int i = 0; i < demCount / size + additionalPage; i++) {

                Page<DemandeBO> demandes = demandesRepository.findAll(PageRequest.of(i, size));
                Page<DemandeEsDTO> demandesEs = demandeEsTransformer.toEs(demandes);

                if (!demandesEs.getContent().isEmpty()) {

                    indexDemandes(demandesEs);
                    indexFiles(demandes);

                }
            }

            LOGGER.info("Fin de la réindexation");
            return demCount;
        }
        LOGGER.info("Fin de la réindexation");
        return 0l;
    }

    /**
     * @see mc.gouv.xaf.back.service.es.IndexedDemandeService#indexDemande(mc.gouv.xaf.back.shared.dto.DemandeDTO)
     */
    @Override
    public void indexDemande(DemandeDTO demandeDTO) throws IOException, SAXException, TikaException, JMSException {

        Boolean activeAccess = accessService.isAccessActive(demandeDTO.getFkAccess());
        DemandeEsDTO demandeEsDTO = demandeEsTransformer.toEs(demandeDTO, activeAccess);
        demandeJmsService.send(new DemandeEsJmsDto(demandeEsDTO, null), JMSActionEnum.SAVE);

    }

    @Override
    public void sendToTopic(DemandeDTO demandeDTO, boolean indexFiles)
            throws IOException, SAXException, TikaException, JMSException {

        if (demandeDTO != null) {

            Boolean activeAccess = accessService.isAccessActive(demandeDTO.getFkAccess());
            DemandeEsDTO demandeEsDTO = demandeEsTransformer.toEs(demandeDTO, activeAccess);

            List<DemandeFileEsDTO> files = null;
            if (indexFiles) {
                files = new ArrayList<>();
                fillFilesList(files, demandeDTO);
                files.addAll(files);
            }

            demandeJmsService.send(new DemandeEsJmsDto(demandeEsDTO, files), JMSActionEnum.SAVE);
        }

    }

    /**
     * @see mc.gouv.xaf.back.service.es.IndexedDemandeService#sendToTopic(mc.gouv.xaf.back.shared.dto.DemandeFileDTO, java.lang.String, java.lang.String)
     */
    @Override
    public void sendToTopic(DemandeFileDTO demandeFileDTO, String demarcheId, String demandeId)
            throws IOException, SAXException, TikaException, JMSException {

        if (demandeFileDTO != null) {

            DemandeFileEsDTO demandeFileEsDTO = getFileEsContent(demarcheId, demandeId,
                    getDemandeFileType(demandeFileDTO), demandeFileDTO);

            List<DemandeFileEsDTO> demFileEsDtoList = new ArrayList<>();
            demFileEsDtoList.add(demandeFileEsDTO);

            demandeJmsService.send(new DemandeEsJmsDto(null, demFileEsDtoList), JMSActionEnum.SAVE);
        }

    }

    /**
     * @see mc.gouv.xaf.back.service.es.IndexedDemandeService#sendToTopic(mc.gouv.xaf.back.shared.dto.DemandeFileDTO, java.lang.String, java.lang.String)
     */
    @Override
    public void sendToTopic(DemandeFileDTO[] demandeFileDTOList, String demarcheId, String demandeId)
            throws IOException, SAXException, TikaException, JMSException {

        if (demandeFileDTOList != null) {

            List<DemandeFileEsDTO> demFileEsDtoList = new ArrayList<>();
            for (DemandeFileDTO file : demandeFileDTOList) {
                demFileEsDtoList.add(getFileEsContent(demarcheId, demandeId, getDemandeFileType(file), file));
            }

            demandeJmsService.send(new DemandeEsJmsDto(null, demFileEsDtoList), JMSActionEnum.SAVE);
        }

    }

    /**
     * Méthode permettant de récupérer une demande de la base et de l'indexer
     * 
     * @see mc.gouv.xaf.back.service.es.IndexedDemandeService#indexDemande(java.lang.String, java.lang.Integer)
     */
    @Override
    public void indexDemande(String demarcheId, Integer demandeId)
            throws IOException, SAXException, TikaException, JMSException {

        DemandeBO demandeBo = getDemandeBo(demarcheId, demandeId);
        DemandeEsDTO demandeEsDTO = demandeEsTransformer.bo2Dto(demandeBo, null);
        demandeJmsService.send(new DemandeEsJmsDto(demandeEsDTO, null), JMSActionEnum.SAVE);

    }

    /**
     * Méthode permettant de récupérer une liste de DTO avec le contenu des fichier sous forme de chaine de caractéres
     * <br/>
     * les contenus des fichiers sont récupérés depuis le web service file
     * 
     * @param demarcheId
     *            Identifinat de la demarche
     * @param demIdentifiant
     *            Identifiant de la demande
     * @param type
     *            Type du fichier
     * @param demandeFileDTOs
     *            Liste des DTOs de fichiers à indexer
     * @return Liste des DTOs des fichiers indexés
     * @throws IOException
     */
    public List<DemandeFileEsDTO> getFileEsContent(String demarcheId, String demIdentifiant, DemandeFileEsDTO.TYPE type,
            List<DemandeFileDTO> demandeFileDTOs) throws IOException {

        List<DemandeFileEsDTO> filesList = new ArrayList<>();

        if (demandeFileDTOs != null) {

            for (DemandeFileDTO demandeFileDTO : demandeFileDTOs) {
                filesList.add(getFileEsContent(demarcheId, demIdentifiant, type, demandeFileDTO));
            }
        }
        return filesList;
    }

    /**
     * Méthode permettant de récupérer un DTO avec le contenu du fichier sous forme de chaine de caractéres <br/>
     * le contenu du fichier est récupéré depuis le web service file
     * 
     * @param demarcheId
     *            Identifiant de la démarche
     * @param demIdentifiant
     *            Identifiant de la demande
     * @param fichier
     *            DTO du fichier à indexé
     * @return Fichier indexé
     * @throws IOException
     */
    private DemandeFileEsDTO getFileEsContent(String demarcheId, String demIdentifiant, DemandeFileEsDTO.TYPE type,
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
                fileUrl = demarcheId + "/" + gouvPropertiesResolver.getContainerId() + "/" + finalFilename;
                LOGGER.info("Le fichier à indexer est le {}", fileUrl);
                is = fileClient.getFile(fileUrl);
            } catch (ConnectException e) {
                throw new FileConnectionException("Could not connect to file", e);
            }
            DemandeFileEsDTO demandeFileEsDTO = new DemandeFileEsDTO(demIdentifiant);
            demandeFileEsDTO.getFichiers().setMeta(fichier.getMeta());
            demandeFileEsDTO.getFichiers().setName(fichier.getName());
            demandeFileEsDTO.getFichiers().setUrl(fichier.getUrl());
            demandeFileEsDTO.getFichiers().setType(type.name());

            if (is != null) {
                String fileText = "";
                try {
                    fileText = FileUtils.parseToPlainText(is);
                    demandeFileEsDTO.getFichiers().setContent(fileText);
                    demandeFileEsDTO.getFichiers().setLanguage(FileUtils.detectLanguage(fileText));

                } catch (ZeroByteFileException e) {
                    LOGGER.info("Le fichier : {} est vide (a une taille de 0 byte)", fileUrl);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            return demandeFileEsDTO;
        }
        return null;

    }

    /**
     * Méthode permettant d'indexer les demandes
     * 
     * @param demandeEsDTOs
     *            Page des demandes à indexer
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
            elasticsearchTemplate.refresh(DemandeEsDTO.class);
        }
        return demandeEsDTOs;
    }

    /**
     * Méthode permettant d'indexer un fichier
     * 
     * @param demandeFileEsDTO
     *            Fichier à indexer
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
     * @param demandeFileEsDTOs
     *            Liste des fichiers à indexer
     * @return Liste des fichiers indexées
     */
    @Override
    public List<DemandeFileEsDTO> indexFiles(List<DemandeFileEsDTO> demandeFileEsDTOs) {

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

    public void bulkIndex(List<IndexQuery> queries) {
        BulkRequestBuilder bulkRequest = elasticsearchTemplate.getClient().prepareBulk();
        for (IndexQuery query : queries) {
            bulkRequest.add(prepareIndex(query));
        }
        checkForBulkUpdateFailure(bulkRequest.execute().actionGet());
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

    private IndexRequestBuilder prepareIndex(IndexQuery query) {
        try {

            IndexRequestBuilder indexRequestBuilder = null;

            if (query.getObject() != null) {
                // If we have a query id and a document id, do not ask ES to generate one.
                indexRequestBuilder = elasticsearchTemplate.getClient().prepareIndex(indexAlias,
                        DemandeEsDTO.INDEX_TYPE, query.getId());
                indexRequestBuilder.setSource(resultsMapper.getEntityMapper().mapToString(query.getObject()),
                        Requests.INDEX_CONTENT_TYPE);
            } else {
                throw new ElasticsearchException(
                        "object or source is null, failed to index the document [id: " + query.getId() + "]");
            }

            indexRequestBuilder.setRouting(query.getParentId());

            return indexRequestBuilder;
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
        initMappingProperties(false);

        if (!StringUtils.isBlank(demandeRecherche.getTexte())) {

            NativeSearchQueryBuilder nativeSearchQueryBuilder = getFacetsAggregationQuery(demandeRecherche);

            return elasticsearchTemplate.query(nativeSearchQueryBuilder.build(), (SearchResponse response) -> {

                DemandesFacets facets = new DemandesFacets();

                if (response.getAggregations().asList().isEmpty()) {
                    return null;
                }

                for (Aggregation agg : response.getAggregations().asList()) {
                    InternalFilters filters = (InternalFilters) agg;
                    for (InternalBucket bucket : filters.getBuckets()) {
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
     * @param demandeRecherche
     *            Paramètres de la recherche
     * @return Query builder avec la requete de récupération des facets
     */
    private NativeSearchQueryBuilder getFacetsAggregationQuery(DemandeRechercheDTO demandeRecherche) {

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withIndices(indexAlias)
                .withQuery(getQueryBuilder(demandeRecherche));

        KeyedFilter[] queryStringQueryBuilders = new KeyedFilter[demandesProperties.size()
                + filesProperties.size() * 3];

        int i = updateFilters(queryStringQueryBuilders, 0, demandeRecherche.getTexte(), false, demandesProperties);
        updateFilters(queryStringQueryBuilders, i, demandeRecherche.getTexte(), true, filesProperties);

        queryStringQueryBuilders = Arrays.stream(queryStringQueryBuilders).filter(Objects::nonNull)
                .toArray(KeyedFilter[]::new);

        if (queryStringQueryBuilders.length > 0) {
            nativeSearchQueryBuilder = nativeSearchQueryBuilder
                    .addAggregation(AggregationBuilders.filters("facets", queryStringQueryBuilders));
        }

        return nativeSearchQueryBuilder;
    }

    /**
     * Méthode permettant de mettre à jour les filtres de la requete qui permet de recupérer les facets
     * 
     * @param queryStringQueryBuilders
     *            Tableau des filtres
     * @param index
     *            Index à partir du quel la mise à jour du tableau des filtres commence
     * @param text
     *            Texte de la barre de recherche
     * @param searchInChild
     *            Boolean permettant d'indiquer si on recheche dans une demande ou dans un fils de la demande (fichier)
     * @param properties
     *            Liste des propriétés du document (demande ou fichier)
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

                    //Ajout du filtre pour les courriers
                    TermQueryBuilder courriersTqb = termQuery(DemandeFileEsDTO.TYPE_FIELD,
                            DemandeFileEsDTO.TYPE.COURRIER.name());

                    BoolQueryBuilder courriersBqb = boolQuery().must(sqsb).must(internalFilestqb);

                    HasChildQueryBuilder courriersHasChildQueryBuilder = hasChildQuery(
                            DemandeFileEsDTO.INDEX_FILES_JOIN_DOC, internalFilesbqb, ScoreMode.Avg);

                    queryStringQueryBuilders[index] = new KeyedFilter(
                            COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX + property.getName(),
                            internalFilesHasChildQueryBuilder);

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
     * @param text Texte de la recherche
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

    /**
     * @see mc.gouv.xaf.back.service.es.IndexedDemandeService#getIndexedDemandes(mc.gouv.xaf.back.shared.dto.DemandeRechercheDTO, org.springframework.data.domain.Pageable, java.lang.String[])
     */
    @Override
    public Page<DemandeEsRechercheDTO> getIndexedDemandes(DemandeRechercheDTO demandeRecherche, Pageable pageable,
            String[] fields) {

        demandeRecherche.setTexte(ESQueryUtils.getFormatedQuery(demandeRecherche.getTexte(),
                afBackUtils.getDemarcheInfos().getIdentifiantPrefixe()));
        initMappingProperties(false);

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
                    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz,
                            Pageable pageable) {
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

                                        boolean isCourrier = type.equals(DemandeFileEsDTO.TYPE.COURRIER.name());

                                        updateHighLightedField(searchInnerHit.getHighlightFields(),
                                                demEsHighlightFields, false, false, isCourrier);
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

    /**
     * Méthode permettant de construire la map ayant comme clé le champ ou la recherche à été faite et comme valeur les
     * fragments contenant le résultat de recherche.<br/>
     * Les mots clés de la recherche sont entourés par des balises qui les mettent en évidence
     * 
     * @param highlightFields
     *            Map des conetant les fragments surlignés récupérée de la recherche elasticsearch
     * @param demEsHighlightFields
     *            Map Contenant les fragments avec les mots clés surlignés associés aux champs ou la recherche a été
     *            effectutée
     * @param isInternalFile
     *            Boolean pour indiquer si on recherche dans les champs d'un fichier de type Fichier interne
     * @param isComplement
     *            Boolean pour indiquer si on recherche dans les champs d'un fichier de type complement
     */
    private void updateHighLightedField(Map<String, HighlightField> highlightFields,
            Map<String, String> demEsHighlightFields, boolean isInternalFile, boolean isComplement, boolean isCourrier) {
        for (Entry<String, HighlightField> entry : highlightFields.entrySet()) {
            Text[] fragments = entry.getValue().fragments();
            if (fragments != null && fragments.length > 0) {
                StringBuilder fragmentField = new StringBuilder(entry.getKey());
                if (propertiesFields.get(fragmentField.toString()) != null) {
                    fragmentField = new StringBuilder(propertiesFields.get(fragmentField.toString()));
                }

                final String fragmentEdge = "...";
                final StringBuilder fragmentSeparation = new StringBuilder(fragmentEdge + "<br/>" + fragmentEdge);

                String fragmentsAsString = Arrays.stream(fragments).map(Objects::toString)
                        .collect(Collectors.joining(fragmentSeparation.toString()));
                StringBuilder fragmentsSB = new StringBuilder(fragmentsAsString);
                if (fragments.length > 1) {
                    fragmentsSB = fragmentsSB.insert(0, fragmentEdge).append(fragmentEdge);
                }

                if (isComplement) {
                    fragmentField = fragmentField.insert(0, FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX);
                } else if (isCourrier) {
                    fragmentField = fragmentField.insert(0, COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX);
                } else if (isInternalFile) {
                    fragmentField = fragmentField.insert(0, INTERNAL_FILE_HIGHLIGHT_AND_FACET_PREFIX);
                }

                demEsHighlightFields.put(fragmentField.toString(), fragmentsSB.toString().replace("'", "&quot;")
                        .replace("\"", "\\\"").replace(highlightPretags.replace("\"", "\\\""), highlightPretags));
            }
        }
    }

    /**
     * Méthode permettant d'initialiser la requete highlight qui identifie les termes recherchés dans le document
     * elasticsearch
     * 
     * @param demandeRecherche
     *            Paramètres de la recherche
     * @param nativeSearchQueryBuilder
     *            Query builder
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
     * @param field Field à highlighter
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
     * @param propertyName
     *            Nom de la propriétés
     * @param properties
     *              liste des propriétés elasticsearch
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
     * Méthode permettant la construction de la requete elasticserach de récupération des demandes
     * 
     * @param demandeRecherche
     *            Paramètres de la recherche
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
     * @param demandeQueryStringQueryBuilder
     *            Requete sur les attributs de la demande
     * @param filesQueryStringQueryBuilder
     *            Requete sur les attributs des fichiers
     * @param rechercheText
     *            Texte de la barre de recherche
     * @param boolQueryBuilder
     *            Requete globale qui combine les requetes sur les demandes et sur les fichiers
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

    /**
     * Méthode permattant la construction de la requete de recupération des demandes lorsque on a cliqué sur une facet
     * 
     * @param demandeQueryStringQueryBuilder
     *            Requete sur les attributs de la demande
     * @param filesQueryStringQueryBuilder
     *            Requete sur les attributs des fichiers
     * @param rechercheText
     *            Texte de la barre de recherche
     * @param searchFields
     *            facet sur lequel on a cliqué
     * @param boolQueryBuilder
     *            Requete globale qui combine les requetes sur les demandes et sur les fichiers
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
     * @param boolQueryBuilder
     *            Requete globale qui combine les requetes sur les demandes, sur les fichiers et sur les filtres
     *            définis dans l'interface graphique
     * @param demandeRecherche
     *            DTO contenant les champs de la recherche (filtres+barre de recherche)
     * @return Requete globale qui combine les requetes sur les demandes, sur les fichiers et sur les filtres définits
     *         dans l'interface graphique
     */
    private BoolQueryBuilder getUiFilterQuery(BoolQueryBuilder boolQueryBuilder, DemandeRechercheDTO demandeRecherche) {

        String statutKey = DemandeEsDTO.DERNIER_STATUT_FIELD_NAME + "." + DemandeStatutEsDTO.CODE_FIELD_NAME
                + ES_KEYWORD;

        if (demandeRecherche.getAucunStatut()) {
            boolQueryBuilder = boolQueryBuilder
                    .mustNot(termsQuery(statutKey, demarchesDataProvider.getStatusMap().keySet()))
                    .must(existsQuery(statutKey));
        } else if (demandeRecherche.getStatuts() != null) {
            boolQueryBuilder = boolQueryBuilder.must(termsQuery(statutKey, demandeRecherche.getStatuts()));
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

        if (!StringUtils.isBlank(demandeRecherche.getAgentAffecteId())) {
            boolQueryBuilder = boolQueryBuilder
                    .must(termQuery(DemandeEsDTO.AGENT_FIELD_NAME + "." + AgentEsDTO.MATRICULE_FIELD_NAME + ES_KEYWORD,
                            demandeRecherche.getAgentAffecteId()));
        }

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

    /**
     * Méthode permettant de sauvgarder une demande et de l'indexer
     * @throws Exception
     *
     * @see mc.gouv.xaf.back.service.data.impl.DemandesServiceImpl#saveDemande(mc.gouv.xaf.back.shared.dto.DemandeDTO, java.lang.String)
     */
    @Override
    public DemandeDTO saveDemande(DemandeDTO demande, String premierStatut) throws Exception {

        DemandeDTO demandeDto = super.saveDemande(demande, premierStatut);
        try {
            sendToTopic(demandeDto, true);
        } catch (TikaException | JMSException e) {
            LOGGER.error(e.getMessage(), e);
            throw new AfIndexingException(e.getMessage(), e);
        }
        return demandeDto;
    }

    /**
     * Méhode permettant de mettre à jour une demande et de la réindexer
     *
     * @see mc.gouv.xaf.back.service.data.impl.DemandesServiceImpl#updateDemande(mc.gouv.xaf.back.shared.dto.DemandeDTO, boolean)
     */
    @Override
    public DemandeDTO updateDemande(DemandeDTO demande, boolean partialUpdate) throws IOException, SAXException {

        DemandeDTO dto = super.updateDemande(demande, partialUpdate);
        try {
            indexDemande(dto);
        } catch (TikaException | JMSException e) {
            LOGGER.error(e.getMessage(), e);
            throw new AfIndexingException(e.getMessage(), e);
        }
        return dto;
    }

    /**
     * Méthode permettant de supprimer une demande et de la supprimer de l'index elasticsearch
     * @see mc.gouv.xaf.back.service.data.impl.DemandesServiceImpl#deleteDemande(java.lang.String, java.lang.Integer)
     */
    @Override
    public void deleteDemande(String demarcheId, Integer demandeId) throws JsonProcessingException, JMSException {

        super.deleteDemande(demarcheId, demandeId);
        Optional<DemandeBO> demandeBoOp = demandesRepository.findById(demandeId);
        if (demandeBoOp.isPresent()) {
            demandeJmsService.send(new DemandeEsJmsDto(new DemandeEsDTO(demandeBoOp.get().getIdentifiant()), null),
                    JMSActionEnum.DELETE);
        }
    }

    /**
     * Méthode permettant de cloner une demande et d'indexer la nouvelle demande
     * 
     * @param demarcheId Identifiant de la démarche
     * @param pkDemande Identifiant de la demande
     * 
     * @return retourne de DTO de la demande
     * 
     * @see mc.gouv.xaf.back.service.data.impl.DemandesServiceImpl#cloneDemande(java.lang.String, java.lang.Integer)
     */
    @Override
    public DemandeDTO cloneDemande(String demarcheId, Integer pkDemande) {

        DemandeDTO demandeDTO = super.cloneDemande(demarcheId, pkDemande);

        try {
            sendToTopic(demandeDTO, true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
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

}

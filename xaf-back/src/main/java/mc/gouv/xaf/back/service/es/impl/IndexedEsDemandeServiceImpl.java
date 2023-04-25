package mc.gouv.xaf.back.service.es.impl;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.simpleQueryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.join.query.JoinQueryBuilders.hasChildQuery;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filters;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator.KeyedFilter;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilters;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.RechercheChampConfigRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.RechercheChampConfigBO;
import mc.gouv.xaf.back.data.es.dao.DemandeEsRepository;
import mc.gouv.xaf.back.data.es.dao.DemandesFilesEsRepository;
import mc.gouv.xaf.back.data.es.model.AgentEsDTO;
import mc.gouv.xaf.back.data.es.model.CanalEsDto;
import mc.gouv.xaf.back.data.es.model.DemandeAccessEsDTO;
import mc.gouv.xaf.back.data.es.model.DemandeEsDTO;
import mc.gouv.xaf.back.data.es.model.DemandeEsRechercheDTO;
import mc.gouv.xaf.back.data.es.model.DemandeFileEsDTO;
import mc.gouv.xaf.back.data.es.model.DemandeFileEsRechercheDTO;
import mc.gouv.xaf.back.data.es.model.DemandeStatutEsDTO;
import mc.gouv.xaf.back.data.es.model.DemandesFacet;
import mc.gouv.xaf.back.data.es.model.DemandesFacets;
import mc.gouv.xaf.back.data.es.model.EsErrorEventDTO;
import mc.gouv.xaf.back.data.es.model.EsProperty;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.exception.AfIndexingException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.impl.DemandesServiceImpl;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.es.IndexedFilesService;
import mc.gouv.xaf.back.service.es.handlers.EsTransactionErrorsHandler;
import mc.gouv.xaf.back.service.es.transformer.DemandeEsTransformer;
import mc.gouv.xaf.back.service.es.utils.EsUtils;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.back.service.utils.ESQueryUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DataRechercheDTO;
import mc.gouv.xaf.shared.dto.DemandeCanalEnum;
import mc.gouv.xaf.shared.dto.DemandeCourrierRechercheDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;

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
    public final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
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
    // Map contenant les champs et le boost (si on veut augmenter le score de la recherche par rapport à un champ)
    // sur lesquels on va faire la recherche du type demandes de l'index <application.name>-index
    private final Map<String, Float> demandesPropertiesWithBoost = new HashMap<>();
    // Map contenant les champs et le boost (si on veut augmenter le score de la recherche par rapport à un champ)
    // sur lesquels on va faire la recherche du type fichiers de l'index <application.name>-index
    private final Map<String, Float> filesPropertiesWithBoost = new HashMap<>();
    private final Map<String, String> propertiesFields = new HashMap<>();
    // Liste des champs à exclure de la recherche des demandes
    private final List<String> demandesFieldsToExclude = new ArrayList<>();
    // Liste des champs à exclure de la recherche dans les fichiers associés aux demandes
    private final List<String> fichiersFieldsToExclude = new ArrayList<>();

    @Inject
    private RechercheChampConfigRepository rechercheChampConfigRepository;
    @Inject
    private DemarchesDataProvider demarchesDataProvider;
    List<EsProperty> allProperties = new ArrayList<>();
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Inject
    private DemandeEsRepository demandeEsRepository;
    @Inject
    private DemandesFilesEsRepository demandesFilesEsRepository;
    @Inject
    private AccessService accessService;
    @Inject
    private DemandeEsTransformer demandeEsTransformer;
    @Inject
    private AfBackUtils afBackUtils;
    @Value("${application.name}")
    private String indexAlias;
    // Balise à insérer au début des mots recherchés dans le résultat de la recherche
    private String highlightPretags;
    // Balise à insérer à la fin des mots recherchés dans le résultat de la recherche
    private String highlightPosttags;
    @Inject
    private DemandesRepository demandesRepository;
    @Inject
    private ElasticsearchOperations elasticsearchTemplate;
    @Inject
    private GouvPropertiesResolver gouvPropertiesResolver;
    @Autowired
    private IndexedFilesService indexedFilesService;

    /**
     * @deprecated RestHighLevelClient est deprecated, il faut remplacer par Elasticsearch Java API Client
     */
    @Inject
    @Deprecated(forRemoval = true)
    private RestHighLevelClient client;

    @PostConstruct
    public void init() {
        highlightPretags = gouvPropertiesResolver.getSearchHighlightPreTags();
        highlightPosttags = gouvPropertiesResolver.getSearchHighlightPostTags();
        loadProperties();
    }

    @Override
    public void loadProperties() {
        try {
            initMappingProperties(true);
        } catch (Exception e) {
            LOGGER.error(
                    "Erreur lors de l'initialisation du mapping elasticsarch: Vérifiez que elasticsearch est bien démarré");
            LOGGER.error(e.getMessage());
        }
    }

    private void reloadPropertiesToExclude() {
        LOGGER.info(
                "Chargement des propriétés de la recherche avancée et désactivation de celles à exclure du mappings elasticserach");
        List<RechercheChampConfigBO> propertiesToExclude = rechercheChampConfigRepository.findByEnabled(false);
        demandesFieldsToExclude.clear();
        fichiersFieldsToExclude.clear();
        if (propertiesToExclude != null) {
            for (RechercheChampConfigBO champConfigBo : propertiesToExclude) {
                if (champConfigBo.getCle().startsWith(FILE_PROPERTIES_PREFIX)
                        || champConfigBo.getCle().startsWith(FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX)
                        || champConfigBo.getCle().startsWith(INTERNAL_FILE_HIGHLIGHT_AND_FACET_PREFIX)
                        || champConfigBo.getCle().startsWith(COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX)) {
                    fichiersFieldsToExclude.add(champConfigBo.getCle());
                } else {
                    demandesFieldsToExclude.add(champConfigBo.getCle());
                }
            }
        }
        demandesFieldsToExclude.addAll(EsUtils.getMappingFichiersOnly());
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
     * <p>
     * TODO remplacer l'utilisation du RestHighLevelClient par Elasticsearch Java API Client
     * </p>
     * <p>
     * Récupération des du mapping à partir d'un alias
     * </p>
     *
     * @param aliasName
     *            Nom de l'alias
     * @return Mapping Elasticsearch
     */
    @SuppressWarnings({ "rawtypes" })
    private Map getMapping(String aliasName) {
        Assert.notNull(aliasName, "No index defined for putMapping()");
        Map mappings;
        try {
            GetIndexRequest request = new GetIndexRequest(aliasName);
            GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);
            String[] indicesNames = response.getIndices();
            if (indicesNames == null || indicesNames.length == 0) {
                throw new AfIndexingException("Impossible de récupérer l'indice : " + aliasName);
            }
            MappingMetadata indexMappings = response.getMappings().get(indicesNames[0]);
            mappings = indexMappings.getSourceAsMap();
        } catch (Exception e) {
            throw new ElasticsearchException(
                    "Error while getting mapping for indexName : " + aliasName + ", " + e.getMessage());
        }
        return mappings;
    }

    /**
     * Méthode permettant d'initialiser les propriétés elasticsearch sur lesquelles on va faire la recherche
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public synchronized void initMappingProperties(boolean reload) {

        Map<String, Map> mapping = getMapping(indexAlias);

        if (reload) {
            clearProperties();
            // refs ##28082 - [BO] Problème résultat affichage d'une recherche avancée > Catégorie Autres
            // On reload les properties sinon dans une archi genTSA la map demandesFieldsToExclude et
            // demandeFilesToExclude ne sont pas alignées sur les deux
            // A moins de restart le BO (qui lui va call le loadProperty pour les deux noeuds)
            reloadPropertiesToExclude();
        }

        if (demandesProperties.isEmpty() || reload) {
            initMappingProperties(demandesProperties, mapping, demandesFieldsToExclude, false);
            initMappingPropertiesMap(demandesProperties, demandesPropertiesWithBoost);
        }

        if (filesProperties.isEmpty() || reload) {
            initMappingProperties(filesProperties, mapping, fichiersFieldsToExclude, true);
            initMappingPropertiesMap(filesProperties, filesPropertiesWithBoost);
        }

        LOGGER.info("Fin de l'initMappingProperties");
    }

    /**
     * Méthode permettant d'initialiser une map des propriétés sur lesquelles on va faire la recherche avec le boost
     * correspondant
     *
     * @param properties
     *            Liste des propriétés
     * @param propertiesWithBoost
     *            Map avec le boost à initialiser
     */
    private void initMappingPropertiesMap(List<EsProperty> properties, Map<String, Float> propertiesWithBoost) {
        for (EsProperty prop : properties) {
            getSearchFields(prop.getName(), properties).forEach(p -> propertiesWithBoost.put(p, 1f));
        }
    }

    /**
     * Méthode permettant de parser le mapping elasticsearch pour avoir la liste des champs, leurs types et leurs sous
     * fields
     *
     * @param properties
     *            Liste des propriétés à remplir
     * @param mapping
     *            Mapping récupéré à partir de l'API elasticsearch
     * @param fieldsToExclude
     *            Les champs qu'on veut pas récupérer
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private synchronized void initMappingProperties(List<EsProperty> properties, Map<String, Map> mapping,
            List<String> fieldsToExclude, boolean isFilesDocs) {
        if (elasticsearchTemplate != null && mapping != null) {
            for (Entry<String, Map> entry : mapping.entrySet()) {
                if (entry.getKey().equals(EsUtils.ES_MAPPING_PROPERTIES_KEY)) {
                    fillProperties(properties, entry.getValue().entrySet(), fieldsToExclude, isFilesDocs);
                } else {
                    Map<String, Map> mappingCheck = mapping.get(entry.getKey());
                    if (mappingCheck != null) {
                        mapping = mappingCheck;
                    }
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void fillProperties(List<EsProperty> properties, Set<Map.Entry<String, Map>> entrySet,
            List<String> fieldsToExclude, boolean isFilesDocs) {
        Set<String> mappingFichiers = EsUtils.getMappingFichiers();
        for (Entry<String, Map> subMapentry : entrySet) {
            String key = subMapentry.getKey();
            if (!fieldsToExclude.contains(key) && (!isFilesDocs || mappingFichiers.contains(key))) {
                properties.add(new EsProperty(subMapentry.getKey()));
                getPropertyName(subMapentry.getValue(), subMapentry.getKey(), properties);
            }
        }
    }

    /**
     * Méthode permettant de parser une propriété à partir de son nom
     *
     * @param map
     *            Map des propriétés
     * @param propertyName
     *            Nom de la propriété
     * @param properties
     *            Liste des propriétés à remplir
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void getPropertyName(Map<String, Map> map, String propertyName, List<EsProperty> properties) {

        if (map == null || map.isEmpty()) {
            return;
        }
        for (Entry<String, Map> entry : map.entrySet()) {
            if (entry.getKey().equals(EsUtils.ES_MAPPING_PROPERTIES_KEY)) {
                getNameFromProperties(entry.getValue().entrySet(), propertyName, properties);
            } else if (entry.getKey().equals(ES_MAPPING_FIELDS_KEY)) {
                getNameFromFields(entry.getValue().entrySet(), propertyName, properties);
            } else if (entry.getKey().equals(ES_MAPPING_TYPE_KEY)) {
                getNameFromTypes(entry, propertyName, properties);
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void getNameFromProperties(Set<Map.Entry<String, Map>> entrySet, String propertyName,
            List<EsProperty> properties) {
        for (Entry<String, Map> subMapentry : entrySet) {
            String newFiledName = propertyName + "." + subMapentry.getKey();
            int fieldIndex = properties.indexOf(new EsProperty(propertyName));
            if (fieldIndex < 0) {
                properties.add(new EsProperty(newFiledName));
            } else {
                properties.set(fieldIndex, new EsProperty(newFiledName));
            }
            getPropertyName(subMapentry.getValue(), newFiledName, properties);
        }
    }

    @SuppressWarnings({ "rawtypes" })
    private void getNameFromFields(Set<Map.Entry<String, Map>> entrySet, String propertyName,
            List<EsProperty> properties) {
        for (Entry<String, Map> subMapentry : entrySet) {
            int fieldIndex = properties.indexOf(new EsProperty(propertyName));
            if (fieldIndex >= 0) {
                properties.get(fieldIndex).addField(subMapentry.getKey());
                propertiesFields.put(propertyName + "." + subMapentry.getKey(), propertyName);
            }
        }
    }

    @SuppressWarnings({ "rawtypes" })
    private void getNameFromTypes(Entry<String, Map> entry, String propertyName, List<EsProperty> properties) {
        String type = (String) ((Object) entry.getValue());
        int fieldIndex = properties.indexOf(new EsProperty(propertyName));
        if (fieldIndex >= 0) {
            // On exclut les champs de type boolean car il faussent la recherche
            if (StringUtils.equals(type, EsProperty.BOOLEAN_TYPE)) {
                properties.remove(properties.get(fieldIndex));
            } else {
                properties.get(fieldIndex).setType(type);
            }
        }
    }

    /**
     * Methode permettant de récupérer la liste des propriétés elasticsearch
     *
     * @return liste des propriétés elasticsearch
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List<EsProperty> getProperties(boolean reload) {
        if (allProperties.isEmpty() || reload) {
            Map<String, Map> mapping = getMapping(indexAlias);
            initMappingProperties(allProperties, mapping, new ArrayList<>(), false);
            initMappingProperties(allProperties, mapping, new ArrayList<>(), true);
        }
        return allProperties;
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
            LOGGER.info("Fin de la réindexation des demandes");
            return demCount;
        }
        LOGGER.info("Fin de la réindexation des demandes");
        return 0L;
    }

    public List<List<String>> getDemandesDesynchro() {
        long demCount = demandesRepository.count();
        List<DemandeBO> demandesBdd = demandesRepository.findAll(PageRequest.of(0, (int) demCount)).toList();
        List<String> identifiantsDemandesBdd = demandesBdd.stream().map(DemandeBO::getIdentifiant)
                .collect(Collectors.toList());

        // TODO Problème avec la requette ES qui retourne les fichiers et les demandes
        String prefixe = afBackUtils.getDemarcheInfos().getIdentifiantPrefixe();
        List<DemandeEsDTO> demandesEs = this.findAllDemandesLazy();
        List<String> identifiantsDemandesEs = demandesEs.stream()
                .filter(d -> d.getPkDemandes() != null && StringUtils.startsWith(d.getIdentifiant(), prefixe))
                .map(DemandeEsDTO::getIdentifiant).collect(Collectors.toList());

        // [0] Demandes présentes dans ES mais pas en BDD
        // [1] Demandes présentes en BDD mais pas dans ES
        List<List<String>> ret = new ArrayList<>();
        List<String> bddMaisPasES = new ArrayList<>(
                CollectionUtils.subtract(identifiantsDemandesBdd, identifiantsDemandesEs));
        List<String> esMaisPasBDD = new ArrayList<>(
                CollectionUtils.subtract(identifiantsDemandesEs, identifiantsDemandesBdd));

        ret.add(esMaisPasBDD);
        ret.add(bddMaisPasES);
        return ret;
    }

    public List<String> reindexDemandesDesynchro() {
        List<String> demandesSync = new ArrayList<>();

        // [0] Demandes présentes dans ES mais pas en BDD
        // [1] Demandes présentes en BDD mais pas dans ES
        List<List<String>> demandesDesynchro = getDemandesDesynchro();

        // Récupération des demandes
        long demCount = demandesRepository.count();
        List<DemandeBO> demandesBdd = demandesRepository.findAll(PageRequest.of(0, (int) demCount)).toList();

        // Supression des demandes dans ES qui ne sont pas en BDD
        for (String idDemande : demandesDesynchro.get(0)) {
            demandeEsRepository.deleteById(idDemande);
            demandesSync.add(idDemande);
        }

        // Indexation des demandes en BDD qui ne sont pas dans ES
        List<DemandeBO> demandesBoASynchro = demandesBdd.stream()
                .filter(d -> demandesDesynchro.get(1).contains(d.getIdentifiant())).collect(Collectors.toList());
        for (DemandeBO demandeBO : demandesBoASynchro) {
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
                indexedFilesService.indexFiles(demandes);
            }
        }
    }

    @Override
    public void indexDemande(DemandeDTO demandeDTO) {
        Boolean activeAccess = accessService.isAccessActive(demandeDTO.getFkAccess());
        DemandeEsDTO demandeEsDTO = demandeEsTransformer.toEs(demandeDTO, activeAccess);
        try {
            demandeEsRepository.save(demandeEsDTO);
        } catch (Exception e) {
            LOGGER.error(SharedMessages.ERREUR_INDEXATION);
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler
                    .createErrorEvent("IndexedEsDemandeServiceImpl - méthode indexDemande()", demandeDTO, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
    }

    @Override
    public void indexElement(DemandeDTO demandeDTO, boolean indexFiles) {
        if (demandeDTO != null) {
            Boolean activeAccess = accessService.isAccessActive(demandeDTO.getFkAccess());
            DemandeEsDTO demandeEsDTO = demandeEsTransformer.toEs(demandeDTO, activeAccess);
            if (demandeEsDTO != null) {
                demandeEsRepository.save(demandeEsDTO);
            }
            if (indexFiles) {
                LOGGER.info("Appel de l'indexation asynchrone des fichers.");
                indexedFilesService.indexFilesAsynchrone(demandeDTO);
            }
        }
        LOGGER.info("Fin de l'indexation de la demande.");
    }

    @Override
    public void indexElements(List<DemandeDTO> demandes) {
        List<DemandeEsDTO> demandesEs = demandeEsTransformer.toEs(demandes);
        demandeEsRepository.saveAll(demandesEs);
        LOGGER.info("Fin de l'indexation des demandes.");
    }

    /**
     * Méthode permettant de récupérer une demande de la base et de l'indexer
     *
     * @see mc.gouv.xaf.back.service.es.IndexedDemandeService#indexDemande(java.lang.String, java.lang.Integer)
     */
    @Override
    public void indexDemande(String demarcheId, Integer demandeId) {
        DemandeBO demandeBo = getCheckDemarcheDemandeBO(demarcheId, demandeId, true);
        DemandeEsDTO demandeEsDTO = demandeEsTransformer.bo2Dto(demandeBo, null);
        try {
            demandeEsRepository.save(demandeEsDTO);
        } catch (Exception e) {
            LOGGER.error(SharedMessages.ERREUR_INDEXATION);
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler
                    .createErrorEvent("IndexedEsDemandeServiceImpl - méthode indexDemande()", demarcheId, demandeId, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
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
            elasticsearchTemplate.bulkIndex(indexList, IndexCoordinates.of(indexAlias));
        }
        return demandeEsDTOs;
    }

    @Override
    public List<DemandeEsDTO> getIndexedDemandes(DemandeRechercheDTO demandeRecherche) {
        demandeRecherche.setTexte(ESQueryUtils.getFormatedQuery(demandeRecherche.getTexte(),
                afBackUtils.getDemarcheInfos().getIdentifiantPrefixe()));
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(getQueryBuilder(demandeRecherche));
        return elasticsearchTemplate.search(nativeSearchQueryBuilder.build(), DemandeEsDTO.class).stream()
                .map(SearchHit::getContent).collect(Collectors.toList());
    }

    @Override
    public DemandesFacets getDemandesFacets(DemandeRechercheDTO demandeRecherche) {

        demandeRecherche.setTexte(ESQueryUtils.getFormatedQuery(demandeRecherche.getTexte(),
                afBackUtils.getDemarcheInfos().getIdentifiantPrefixe()));
        initMappingProperties(true);
        DemandesFacets facets = new DemandesFacets();

        if (!StringUtils.isBlank(demandeRecherche.getTexte())) {
            NativeSearchQueryBuilder builder = getFacetsAggregationQuery(demandeRecherche);
            SearchHits<DemandeEsRechercheDTO> searchHits = elasticsearchTemplate.search(builder.build(),
                    DemandeEsRechercheDTO.class);

            ElasticsearchAggregations elasticsearchAggregations = (ElasticsearchAggregations) searchHits
                    .getAggregations();
            if (elasticsearchAggregations == null) {
                return null;
            }

            List<Aggregation> aggregations = elasticsearchAggregations.aggregations().asList();
            if (aggregations.isEmpty()) {
                return null;
            }

            for (Aggregation agg : aggregations) {
                ParsedFilters filters = (ParsedFilters) agg;
                for (Filters.Bucket bucket : filters.getBuckets()) {
                    if (bucket.getDocCount() > 0) {
                        facets.add(new DemandesFacet(bucket.getKeyAsString(), bucket.getDocCount()));
                    }
                }
            }
        }

        return facets;
    }

    /**
     * Méthode permettant de récupérer la requete qui construit les facets
     *
     * @param demandeRecherche
     *            Paramètres de la recherche
     * @return Query builder avec la requete de récupération des facets
     */
    private NativeSearchQueryBuilder getFacetsAggregationQuery(DemandeRechercheDTO demandeRecherche) {

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(getQueryBuilder(demandeRecherche));

        List<KeyedFilter> queryStringQueryBuilders = new ArrayList<>();
        updateFilters(queryStringQueryBuilders, demandeRecherche.getTexte(), demandesProperties, false);
        updateFilters(queryStringQueryBuilders, demandeRecherche.getTexte(), filesProperties, true);

        if (CollectionUtils.isEmpty(queryStringQueryBuilders)) {
            return nativeSearchQueryBuilder;
        }
        FiltersAggregationBuilder aggregationBuilder = AggregationBuilders.filters("facets",
                queryStringQueryBuilders.toArray(new KeyedFilter[0]));
        return nativeSearchQueryBuilder.withAggregations(aggregationBuilder);
    }

    /**
     * Méthode permettant de mettre à jour les filtres de la requete qui permet de recupérer les facets
     *
     * @param queryStringQueryBuilders
     *            Tableau des filtres
     * @param text
     *            Texte de la barre de recherche
     * @param properties
     *            Liste des propriétés du document (demande ou fichier)
     * @param searchInChild
     *            Boolean permettant d'indiquer si on recheche dans une demande ou dans un fils de la demande (fichier)
     */
    private void updateFilters(List<KeyedFilter> queryStringQueryBuilders, String text, List<EsProperty> properties,
            boolean searchInChild) {
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
                    // #47743
                    // Ajout du filtre et de la child query pour les piéces jointes
                    addFileFilters(queryStringQueryBuilders, sqsb, FILE_PROPERTIES_PREFIX + property.getName(),
                            DemandeFileEsDTO.TYPE.PIECE_JOINTE.name());

                    // Ajout du filtre et de la child query pour les complements de demandes
                    addFileFilters(queryStringQueryBuilders, sqsb,
                            FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX + property.getName(),
                            DemandeFileEsDTO.TYPE.COMPLEMENT.name());

                    // Ajout du filtre et de la child query pour les fichiers internes
                    addFileFilters(queryStringQueryBuilders, sqsb,
                            INTERNAL_FILE_HIGHLIGHT_AND_FACET_PREFIX + property.getName(),
                            DemandeFileEsDTO.TYPE.FICHIER_INTERNE.name());

                    // Ajout du filtre et de la child query pour les courriers
                    addFileFilters(queryStringQueryBuilders, sqsb,
                            COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX + property.getName(),
                            DemandeFileEsDTO.TYPE.COURRIER.name());
                } else {
                    queryStringQueryBuilders.add(new KeyedFilter(property.getName(), sqsb));
                }
            }
        }
    }

    /**
     * Méthode permettant d'ajouter les filtres à la query pour récuperer les fichiers
     * 
     * @deprecated les jointures seront supprimées dans ES8
     */
    @Deprecated
    private void addFileFilters(List<KeyedFilter> queryStringQueryBuilders, SimpleQueryStringBuilder sqsb,
            String propertyName, String propertyType) {
        if (!fichiersFieldsToExclude.contains(propertyName)) {
            TermQueryBuilder termQueryBuilder = termQuery(EsUtils.TYPE_FILE_FIELD, propertyType);
            BoolQueryBuilder boolQueryBuilder = boolQuery().must(sqsb).must(termQueryBuilder);
            HasChildQueryBuilder hasChildQueryBuilder = hasChildQuery(EsUtils.INDEX_FILES_JOIN_DOC, boolQueryBuilder,
                    ScoreMode.Avg);
            queryStringQueryBuilders.add(new KeyedFilter(propertyName, hasChildQueryBuilder));
        }
    }

    /**
     * Méthode permettant de construire le SimpleQueryStringBuilder permettant de faire la requete de recherche sur tous
     * les champs en paramètres
     *
     * @param text
     *            Texte de la recherche
     * @param fields
     *            Les fields sur lesquels on va faire la recherche
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

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(getQueryBuilder(demandeRecherche)).withPageable(pageable);

        nativeSearchQueryBuilder = highlightQuery(demandeRecherche, nativeSearchQueryBuilder);
        if (fields != null && fields.length > 0) {
            SourceFilter sourceFilter = new FetchSourceFilter(fields, null);
            nativeSearchQueryBuilder.withSourceFilter(sourceFilter);
        }

        NativeSearchQuery query = nativeSearchQueryBuilder.build();
        SearchHits<DemandeEsRechercheDTO> searchHits = elasticsearchTemplate.search(query, DemandeEsRechercheDTO.class);
        return aggregateResults(searchHits, pageable);
    }

    /**
     * Transforme l'objet retourné par la recherche en Page
     *
     * @param searchHits,
     *            Objet contenant les résultats de recherche
     * @param pageable,
     *            Objet contenant les informations sur la page à retourner
     * @return Page
     */
    private Page<DemandeEsRechercheDTO> aggregateResults(SearchHits<DemandeEsRechercheDTO> searchHits,
            Pageable pageable) {
        if (searchHits.isEmpty()) {
            return Page.empty(pageable);
        }

        List<DemandeEsRechercheDTO> demandesEsList = new ArrayList<>();
        for (SearchHit<DemandeEsRechercheDTO> searchHit : searchHits) {
            DemandeEsRechercheDTO demandeEsRechercheDTO = searchHit.getContent();
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            Map<String, String> demEsHighlightFields = new HashMap<>();
            updateHighLightedFieldList(highlightFields, demEsHighlightFields, false, false, false);
            Map<String, SearchHits<?>> innerHits = searchHit.getInnerHits();
            aggregateInnerFields(innerHits, demEsHighlightFields);
            demandeEsRechercheDTO.setHighlightedField(demEsHighlightFields);
            demandesEsList.add(demandeEsRechercheDTO);
        }

        return new PageImpl<>(demandesEsList, pageable, searchHits.getTotalHits());
    }

    /**
     * Méthode ajoutant les mots trouvées dans les fichiers lors de la recherche
     *
     * @param innerHits,
     *            une Map contenant les mots trouvés dans les fichiers
     * @param demEsHighlightFields,
     *            la map contenant tous les résultats de la recherche
     */
    private void aggregateInnerFields(Map<String, SearchHits<?>> innerHits, Map<String, String> demEsHighlightFields) {
        for (Entry<String, SearchHits<?>> searchHitsEntry : innerHits.entrySet()) {
            SearchHits<?> searchHitsArray = searchHitsEntry.getValue();
            for (SearchHit<?> searchInnerHit : searchHitsArray) {
                DemandeEsRechercheDTO content = (DemandeEsRechercheDTO) searchInnerHit.getContent();
                String type = content.getTypeFichier();
                boolean isInternalFile = type.equals(DemandeFileEsDTO.TYPE.FICHIER_INTERNE.name());
                boolean isComplement = type.equals(DemandeFileEsDTO.TYPE.COMPLEMENT.name());
                boolean isCourrier = type.equals(DemandeFileEsDTO.TYPE.COURRIER.name());
                updateHighLightedFieldList(searchInnerHit.getHighlightFields(), demEsHighlightFields, isInternalFile,
                        isComplement, isCourrier);
            }
        }
    }

    @Override
    // TODO Sortir cette méthode dans IndexedEsDemandeFilesServiceImpl, afin de regrouper les actions sur les fichiers
    public Page<DemandeFileEsRechercheDTO> getIndexedCourriers(DemandeCourrierRechercheDTO demandeRecherche,
            Pageable pageable, String[] fields) {

        demandeRecherche.setTexte(ESQueryUtils.getFormatedQuery(demandeRecherche.getTexte(),
                afBackUtils.getDemarcheInfos().getIdentifiantPrefixe()));
        initMappingProperties(false);

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(getQueryBuilderForCourrier(demandeRecherche)).withPageable(pageable);

        nativeSearchQueryBuilder = highlightQuery(demandeRecherche, nativeSearchQueryBuilder);
        if (fields != null && fields.length > 0) {
            SourceFilter sourceFilter = new FetchSourceFilter(fields, null);
            nativeSearchQueryBuilder.withSourceFilter(sourceFilter);
        }

        NativeSearchQuery query = nativeSearchQueryBuilder.build();
        SearchHits<DemandeFileEsRechercheDTO> searchHits = elasticsearchTemplate.search(query,
                DemandeFileEsRechercheDTO.class);
        return aggregateResultsCourriers(searchHits, pageable);

    }

    /**
     * Transforme l'objet retourné par la recherche courriers en Page
     *
     * @param searchHits,
     *            Objet contenant les résultats de recherche
     * @param pageable,
     *            Objet contenant les informations sur la page à retourner
     * @return Page
     */
    // TODO Sortir cette méthode dans IndexedEsDemandeFilesServiceImpl, afin de regrouper les actions sur les fichiers
    private Page<DemandeFileEsRechercheDTO> aggregateResultsCourriers(SearchHits<DemandeFileEsRechercheDTO> searchHits,
            Pageable pageable) {
        if (searchHits.isEmpty()) {
            return Page.empty(pageable);
        }

        List<DemandeFileEsRechercheDTO> demandesEsList = new ArrayList<>();
        for (SearchHit<DemandeFileEsRechercheDTO> searchHit : searchHits) {
            DemandeFileEsRechercheDTO fichierJoinEsRechercheDTO = searchHit.getContent();
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            Map<String, String> demEsHighlightFields = new HashMap<>();
            updateHighLightedFieldList(highlightFields, demEsHighlightFields, false, false, false);

            Map<String, SearchHits<?>> innerHits = searchHit.getInnerHits();
            aggregateInnerFieldsCourriers(innerHits, demEsHighlightFields);

            fichierJoinEsRechercheDTO.setHighlightedField(demEsHighlightFields);

            demandesEsList.add(fichierJoinEsRechercheDTO);
        }

        return new PageImpl<>(demandesEsList, pageable, searchHits.getTotalHits());
    }

    /**
     * Méthode ajoutant les mots trouvées dans les fichiers lors de la recherche courriers
     *
     * @param innerHits,
     *            une Map contenant les mots trouvés dans les fichiers
     * @param demEsHighlightFields,
     *            la map contenant tous les résultats de la recherche
     */
    // TODO Sortir cette méthode dans IndexedEsDemandeFilesServiceImpl, afin de regrouper les actions sur les fichiers
    private void aggregateInnerFieldsCourriers(Map<String, SearchHits<?>> innerHits,
            Map<String, String> demEsHighlightFields) {
        for (Entry<String, SearchHits<?>> searchHitsEntry : innerHits.entrySet()) {
            SearchHits<?> searchHitsArray = searchHitsEntry.getValue();
            for (SearchHit<?> searchInnerHit : searchHitsArray) {
                DemandeEsRechercheDTO content = (DemandeEsRechercheDTO) searchInnerHit.getContent();
                String type = content.getTypeFichier();
                boolean isCourrier = type.equals(DemandeFileEsDTO.TYPE.COURRIER.name());
                updateHighLightedFieldList(searchInnerHit.getHighlightFields(), demEsHighlightFields, false, false,
                        isCourrier);
            }
        }
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
    private void updateHighLightedFieldList(Map<String, List<String>> highlightFields,
            Map<String, String> demEsHighlightFields, boolean isInternalFile, boolean isComplement,
            boolean isCourrier) {
        for (Entry<String, List<String>> entry : highlightFields.entrySet()) {
            List<String> fragments = entry.getValue();
            if (!fragments.isEmpty()) {
                String fragmentField = getFragmentField(entry.getKey(), isInternalFile, isComplement, isCourrier);

                // Vérification du champs
                if (fichiersFieldsToExclude.contains(fragmentField)) {
                    // On ne veut pas afficher ce champs, donc on continue la boucle for
                    continue;
                }

                final String fragmentEdge = "...";
                final String fragmentSeparation = fragmentEdge + "<br/>" + fragmentEdge;
                String fragmentsAsString = fragments.stream().collect(Collectors.joining(fragmentSeparation));
                StringBuilder fragmentsSB = new StringBuilder(fragmentsAsString);
                if (fragments.size() > 1) {
                    fragmentsSB.insert(0, fragmentEdge).append(fragmentEdge);
                }

                demEsHighlightFields.put(fragmentField, fragmentsSB.toString().replace("'", "&quot;")
                        .replace("\"", "\\\"").replace(highlightPretags.replace("\"", "\\\""), highlightPretags));
            }
        }
    }

    private String getFragmentField(String champs, boolean isInternalFile, boolean isComplement, boolean isCourrier) {
        // Construction du nom du champs
        StringBuilder fragmentFieldBuilder = new StringBuilder(
                (propertiesFields.get(champs) != null) ? propertiesFields.get(champs) : champs);

        // Ajout du préfixe
        if (isComplement) {
            fragmentFieldBuilder.insert(0, FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX);
        } else if (isCourrier) {
            fragmentFieldBuilder.insert(0, COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX);
        } else if (isInternalFile) {
            fragmentFieldBuilder.insert(0, INTERNAL_FILE_HIGHLIGHT_AND_FACET_PREFIX);
        }

        return fragmentFieldBuilder.toString();
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
     * @param field
     *            Field à highlighter
     * @param searchText
     *            Recherche à faire
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
     *            liste des propriétés elasticsearch
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
     * @param demandeRecherche
     *            Paramètres de la recherche
     * @return Requete elasticsearch pour récupérer les demandes
     */
    // TODO Sortir cette méthode dans IndexedEsDemandeFilesServiceImpl, afin de regrouper les actions sur les fichiers
    private BoolQueryBuilder getQueryBuilderForCourrier(DemandeCourrierRechercheDTO demandeRecherche) {

        BoolQueryBuilder boolQueryBuilder = boolQuery();
        TermQueryBuilder tqb = termQuery(EsUtils.TYPE_FILE_FIELD, DemandeFileEsDTO.TYPE.COURRIER.name());
        boolQueryBuilder.must(tqb);

        if (!StringUtils.isBlank(demandeRecherche.getTexte())) {
            SimpleQueryStringBuilder filesQueryStringQueryBuilder = getSimpleQueryStringBuilder(
                    demandeRecherche.getTexte(), null);

            boolQueryBuilder = getQueryWhereForCourriers(filesQueryStringQueryBuilder, demandeRecherche,
                    demandeRecherche.getSearchFields());
        }

        if (demandeRecherche.getImprime()) {
            boolQueryBuilder.must(QueryBuilders.existsQuery(EsUtils.DATE_PRINTED_FILE_FIELD));
        } else {
            boolQueryBuilder.mustNot(QueryBuilders.existsQuery(EsUtils.DATE_PRINTED_FILE_FIELD));
        }

        return getUiFilterQuery(boolQueryBuilder, demandeRecherche);
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
     * Méthode permattant la construction de la requete de recupération des demandes lorsque on n'a pas cliqué sur
     * aucune facet
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
                .setStoredFieldNames(Arrays.asList(EsUtils.TYPE_FILE_FIELD));
        HasChildQueryBuilder hasChildQueryBuilder = hasChildQuery(EsUtils.INDEX_FILES_JOIN_DOC,
                filesQueryStringQueryBuilder, ScoreMode.Avg).innerHit(ihb);
        return boolQueryBuilder.minimumShouldMatch(1).should(demandeQueryStringQueryBuilder)
                .should(hasChildQueryBuilder);
    }

    /**
     * Méthode permettant de construire une requête pour récupérer les courriers.
     *
     * @param filesQueryStringQueryBuilder
     *            Requete sur les attributs des fichiers
     * @param recherche
     *            Texte de la barre de recherche
     * @param searchFields
     *            Liste de paramètre sur lesquels faire la recherche
     * @return Requête sur les courriers
     */
    // TODO Sortir cette méthode dans IndexedEsDemandeFilesServiceImpl, afin de regrouper les actions sur les fichiers
    private BoolQueryBuilder getQueryWhereForCourriers(SimpleQueryStringBuilder filesQueryStringQueryBuilder,
            DemandeCourrierRechercheDTO recherche, String[] searchFields) {
        BoolQueryBuilder boolQueryBuilder = boolQuery();
        TermQueryBuilder tqb = termQuery(EsUtils.TYPE_FILE_FIELD, DemandeFileEsDTO.TYPE.COURRIER.name());
        boolQueryBuilder.must(tqb);

        // Construction des propriétés des courriers
        // TODO Revoir les propriétés liées au courriers notamment sur le statut de la demande, et la date de réception
        // du courrier
        // #41972 - Quickfix : les courriers ont à la fois des propriétés contenus dans les demandes et les fichiers
        List<EsProperty> properties = new ArrayList<>(demandesProperties);
        properties.addAll(filesProperties);
        List<String> searchFilesFields = getSearchFields(searchFields, properties);

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
            String replacedSearchField = searchField;
            if (searchField.startsWith(FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX)) {
                replacedSearchField = searchField.replaceFirst(FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX, "");
                tqb = termQuery(EsUtils.TYPE_FILE_FIELD, DemandeFileEsDTO.TYPE.COMPLEMENT.name());
                boolQueryBuilder.must(hasChildQuery(EsUtils.INDEX_FILES_JOIN_DOC, tqb, ScoreMode.Avg));
            } else if (searchField.startsWith(FILE_PROPERTIES_PREFIX)) {
                tqb = termQuery(EsUtils.TYPE_FILE_FIELD, DemandeFileEsDTO.TYPE.PIECE_JOINTE.name());
                boolQueryBuilder.must(hasChildQuery(EsUtils.INDEX_FILES_JOIN_DOC, tqb, ScoreMode.Avg));
            } else if (searchField.startsWith(INTERNAL_FILE_HIGHLIGHT_AND_FACET_PREFIX)) {
                replacedSearchField = searchField.replaceFirst(INTERNAL_FILE_HIGHLIGHT_AND_FACET_PREFIX, "");
                tqb = termQuery(EsUtils.TYPE_FILE_FIELD, DemandeFileEsDTO.TYPE.FICHIER_INTERNE.name());
                boolQueryBuilder.must(hasChildQuery(EsUtils.INDEX_FILES_JOIN_DOC, tqb, ScoreMode.Avg));
            } else if (searchField.startsWith(COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX)) {
                replacedSearchField = searchField.replaceFirst(COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX, "");
                tqb = termQuery(EsUtils.TYPE_FILE_FIELD, DemandeFileEsDTO.TYPE.COURRIER.name());
                boolQueryBuilder.must(hasChildQuery(EsUtils.INDEX_FILES_JOIN_DOC, tqb, ScoreMode.Avg));
            }
            replacedSearchFields.add(replacedSearchField);
        }

        boolQueryBuilder = boolQueryBuilder.minimumShouldMatch(1);

        List<String> searchDemandeFields = getSearchFields(replacedSearchFields.toArray(new String[0]),
                demandesProperties);
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
                    .setStoredFieldNames(Arrays.asList(EsUtils.TYPE_FILE_FIELD));
            filesQueryStringQueryBuilder = filesQueryStringQueryBuilder.fields(filesFields);
            HasChildQueryBuilder hasChildQueryBuilder;
            if (tqb != null) {
                BoolQueryBuilder bqb = boolQuery().must(filesQueryStringQueryBuilder).must(tqb);
                hasChildQueryBuilder = hasChildQuery(EsUtils.INDEX_FILES_JOIN_DOC, bqb, ScoreMode.Avg).innerHit(ihb);
            } else {
                hasChildQueryBuilder = hasChildQuery(EsUtils.INDEX_FILES_JOIN_DOC, filesQueryStringQueryBuilder,
                        ScoreMode.Avg).innerHit(ihb);
            }

            boolQueryBuilder = boolQueryBuilder.should(hasChildQueryBuilder);

        }
        return boolQueryBuilder;
    }

    /**
     * Méthode permettant la construction de la requete elasticsearch à partir des filtres de la recherche avancée
     *
     * @param boolQueryBuilder
     *            Requete globale qui combine les requetes sur les demandes, sur les fichiers et sur les filtres définis
     *            dans l'interface graphique
     * @param demandeRecherche
     *            DTO contenant les champs de la recherche (filtres+barre de recherche)
     * @return Requete globale qui combine les requetes sur les demandes, sur les fichiers et sur les filtres définits
     *         dans l'interface graphique
     */
    private BoolQueryBuilder getUiFilterQuery(BoolQueryBuilder boolQueryBuilder, DemandeRechercheDTO demandeRecherche) {

        boolQueryBuilder = this.updateBoolQueryBuilderForStatut(boolQueryBuilder, demandeRecherche);

        String canauxKey = DemandeEsDTO.CANAL_FIELD_NAME + "." + CanalEsDto.CANAL_CODE_FIELD_NAME + ES_KEYWORD;

        if (demandeRecherche.getAucunCanal()) {
            boolQueryBuilder = boolQueryBuilder.mustNot(termsQuery(canauxKey,
                    Arrays.stream(DemandeCanalEnum.values()).map(DemandeCanalEnum::name).collect(Collectors.toList())))
                    .must(existsQuery(canauxKey));
        } else if (demandeRecherche.getCanaux() != null) {
            boolQueryBuilder = boolQueryBuilder.must(termsQuery(canauxKey,
                    demandeRecherche.getCanaux().stream().map(DemandeCanalEnum::name).collect(Collectors.toList())));
        }

        if (DemarchesUtils.isFrontUser()) {
            boolQueryBuilder = boolQueryBuilder
                    .must(termQuery(DemandeEsDTO.ACCESS_FIELD_NAME + "." + DemandeAccessEsDTO.ACTIVE_FIELD_NAME, true));
        }

        if (demandeRecherche.isCheckTimestamp()) {
            RangeQueryBuilder timestampQueryBuilder = rangeQuery("modificationTimestamp");
            timestampQueryBuilder = timestampQueryBuilder.lte(Instant.now().toEpochMilli());
            timestampQueryBuilder = timestampQueryBuilder.gte(0L);
            boolQueryBuilder = boolQueryBuilder.must(boolQuery()
                    .should(boolQuery().mustNot(existsQuery("modificationTimestamp"))).should(timestampQueryBuilder));

        }

        if (demandeRecherche.isAucunResponsable()) {
            boolQueryBuilder = boolQueryBuilder.mustNot(
                    existsQuery(DemandeEsDTO.AGENT_FIELD_NAME + "." + AgentEsDTO.MATRICULE_FIELD_NAME + ES_KEYWORD));
        } else if (!StringUtils.isBlank(demandeRecherche.getAgentAffecteId())) {
            boolQueryBuilder = boolQueryBuilder
                    .must(termQuery(DemandeEsDTO.AGENT_FIELD_NAME + "." + AgentEsDTO.MATRICULE_FIELD_NAME + ES_KEYWORD,
                            demandeRecherche.getAgentAffecteId()));
        }

        RangeQueryBuilder rangeQueryBuilder = rangeQuery(DemandeEsDTO.DATE_DEMANDE_FIELD_NAME).format(DATE_PATTERN);

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
        if (demandeRecherche.getData() != null) {
            boolQueryBuilder = this.updateBoolQueryBuilderForData(boolQueryBuilder, demandeRecherche.getData());
        }

        return boolQueryBuilder;
    }

    private BoolQueryBuilder updateBoolQueryBuilderForStatut(BoolQueryBuilder boolQueryBuilder,
            DemandeRechercheDTO demandeRecherche) {
        String statutKey = DemandeEsDTO.DERNIER_STATUT_FIELD_NAME + "." + DemandeStatutEsDTO.CODE_FIELD_NAME
                + ES_KEYWORD;

        if (demandeRecherche.getAucunStatut()) {
            boolQueryBuilder = boolQueryBuilder
                    .mustNot(termsQuery(statutKey, demarchesDataProvider.getStatusMap().keySet()))
                    .must(existsQuery(statutKey));
        } else if (demandeRecherche.getStatuts() != null) {
            if (StringUtils.isNotBlank(demandeRecherche.getStatutPublicOuInterne())) {

                TermsQueryBuilder statutsQ = QueryBuilders.termsQuery(statutKey, demandeRecherche.getStatuts());
                MatchQueryBuilder statutPublicOuInterneQ = QueryBuilders.matchQuery("statutPublicOuInterne",
                        demandeRecherche.getStatutPublicOuInterne());
                BoolQueryBuilder shouldQ = QueryBuilders.boolQuery().should(statutsQ).should(statutPublicOuInterneQ);
                boolQueryBuilder = boolQueryBuilder.must(shouldQ);
            } else {
                boolQueryBuilder = boolQueryBuilder.must(termsQuery(statutKey, demandeRecherche.getStatuts()));
            }
        } else if (StringUtils.isNotBlank(demandeRecherche.getStatutPublicOuInterne())) {
            boolQueryBuilder = boolQueryBuilder
                    .must(matchQuery("statutPublicOuInterne", demandeRecherche.getStatutPublicOuInterne()));
        }
        return boolQueryBuilder;
    }

    private BoolQueryBuilder updateBoolQueryBuilderForData(BoolQueryBuilder boolQueryBuilder,
            DataRechercheDTO dataRechercheDTO) {
        // Pour le moment nous faisons un OU sur les data pour remonter
        // Les demandes en cours de traitement ET sur un agent OU data.IS_EN_ATTENTE_TRAITEMENT=1
        // En attendant un vrai service de recherche ou on pourra définir les OU / ET via json body (comme ES par
        // exemple)

        boolean predicatAnd = dataRechercheDTO.getOperand() != null
                && dataRechercheDTO.getOperand().equals(DataRechercheDTO.DataRechercheOperand.AND);

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
                tmpQB = tmpQB
                        .should(termQuery(DemandeEsDTO.DATA_FIELD_NAME + "." + dataRechercheDTO.getKey() + ES_KEYWORD,
                                dataRechercheDTO.getValue()));
                boolQueryBuilder = tmpQB;
            }

        }
        return boolQueryBuilder;
    }

    @Override
    public DemandeDTO saveDemande(DemandeDTO demande, String premierStatut) throws IOException {
        DemandeDTO demandeDto = super.saveDemande(demande, premierStatut);
        try {
            indexElement(demandeDto, true);
        } catch (Exception e) {
            LOGGER.error("Erreur d'indexation lors de la sauvegarde de la demande.");
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler
                    .createErrorEvent("IndexedEsDemandeServiceImpl - méthode saveDemande()", demandeDto, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
        return demandeDto;
    }

    /**
     * Méhode permettant de mettre à jour une demande et de la réindexer
     */
    @Override
    public DemandeDTO updateDemande(DemandeDTO demande, boolean partialUpdate) {
        DemandeDTO demandeDTO = super.updateDemande(demande, partialUpdate);
        try {
            indexDemande(demandeDTO);
        } catch (Exception e) {
            LOGGER.error("Erreur d'indexation lors de l'update de la demande.");
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler
                    .createErrorEvent("IndexedEsDemandeServiceImpl - méthode updateDemande()", demandeDTO, e);
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
        LOGGER.info("Début de suppression des références des fichiers de la demande {} dans Elasticsearch...",
                demandeId);
        try {
            deleteDemandeInGivenStatus(demarcheId, demandeId, new ArrayList<>(), -1);
        } catch (Exception e) {
            LOGGER.error("Erreur d'indexation lors de la suppression de la demande.");
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent(
                    "IndexedEsDemandeServiceImpl - méthode deleteDemande()", demarcheId, demandeId, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
    }

    private void deleteEsFilesIndex(Integer demandeId, DemandeDTO demandeDTO) {
        if (null != demandeDTO.getFichiers()) {
            List<DemandeFileDTO> filesToDelete = Arrays.asList(demandeDTO.getFichiers());
            // On supprime les index des fichiers de la demande dans ES
            if (!filesToDelete.isEmpty()) {
                List<String> idsToDelete = new ArrayList<>();
                for (DemandeFileDTO currentFileToDelete : filesToDelete) {
                    // L'identifiant ES est formé à partir de l'url du fichier
                    String identifiantFile = currentFileToDelete.getUrl().replace("/", "-");
                    // Ici le format de l'ID d'un courrier dans ES est pkDemande-identifiant
                    String currentFileEsId = demandeId + "-" + identifiantFile;
                    // On ajoute à la liste d'ids à supprimer
                    idsToDelete.add(currentFileEsId);
                }
                // Puis on appel le repo pour supprimer les fichiers
                LOGGER.info("Début suppression des fichiers : {} dans ElasticSearch", idsToDelete);
                demandesFilesEsRepository.deleteAllById(idsToDelete);
                LOGGER.info("Fin suppression des fichiers : {} dans ElasticSearch", idsToDelete);
            }
        }
    }

    /**
     * Méthode permettant de supprimer une demande à purger avec une liste de status compatible à la supression (statuts
     * finaux) et de la supprimer de l'index elasticsearch
     *
     * @see mc.gouv.xaf.back.service.data.impl.DemandesServiceImpl#deleteDemandeInGivenStatus(String, Integer, List,
     *      int)
     */
    @Override
    public void deleteDemandeInGivenStatus(String demarcheId, Integer demandeId, List<String> statuts, int jours)
            throws JsonProcessingException {
        LOGGER.info("Début de suppression des références des fichiers de la demande {} dans Elasticsearch...",
                demandeId);
        try {
            DemandeBO demandeBo = getCheckDemarcheDemandeBO(demarcheId, demandeId, false);
            DemandeDTO demandeDTO = DemandesTransformer.bo2Dto(demandeBo);
            // On supprime l'index des fichiers de la demande dans ES
            deleteEsFilesIndex(demandeId, demandeDTO);
            // Puis on supprime l'index de la demande elle même dans ES
            demandeEsRepository.deleteById(demandeBo.getIdentifiant());
            /*
             * Cette méthode n'étant pas le point d'entrée du TraitementController de chaque TS il a fallut mettre en
             * place une logique spécifique
             *
             * Le traitement controller de chaque demande va appeler deleteDemande qui lui va appeler
             * deleteDemandeInGivenStatus avec une liste de statuts vides et jours < 0 (deleteDemande est utilisé si
             * erreur au moment de la création/duplication d'une demande) Dans ce cas là, le deleteDemande va supprimer
             * les fichiers rattachés à cette demande sans tests préalable.
             */
            if (statuts.isEmpty() && jours < 0) {
                super.deleteDemande(demarcheId, demandeId);
            } else {
                /*
                 * Lors de l'appel a ce super.deleteDemandeInGivenStatus, un test sera fait en amont pour juger si oui
                 * ou non les fichiers rattachés à cette demande sont supprimables Les fichiers rattachés à une demande
                 * d'origine sont les mêmes (DANS FILE) que les fichiers des demandes dupliquées à partir de l'initiale.
                 * Il faut donc veiller à ce que plus personne n'ait besoin de ces fichiers dans file avant de les
                 * supprimer
                 */
                super.deleteDemandeInGivenStatus(demarcheId, demandeId, statuts, jours);
            }
        } catch (Exception e) {
            LOGGER.error("Erreur d'indexation lors de la suppression de la demande.");
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent(
                    "IndexedEsDemandeServiceImpl - méthode deleteDemande()", demarcheId, demandeId, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
    }

    /**
     * Méthode permettant de cloner une demande et d'indexer la nouvelle demande
     *
     * @param demarcheId
     *            Identifiant de la démarche
     * @param pkDemande
     *            Identifiant de la demande
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
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler
                    .createErrorEvent("IndexedEsDemandeServiceImpl - méthode cloneDemande()", demandeDTO, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
        return demandeDTO;
    }

    /**
     * Méthode permettant de formatter une date au format 'dd/MM/yyyy'
     *
     * @param date
     *            La date à formatter
     * @return la date formattée
     */
    private String getFormatedDate(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        return new SimpleDateFormat(DATE_PATTERN).format(cal.getTime());
    }

    /**
     * Récupère uniquement l'identifiant et la pkDemandes de tous les documents de l'index ES
     *
     * @return List des demandes en Lazy
     *         <p>
     *         TODO Bug: depuis les changements avec la migration ES, les fichiers et les demandes remontent dans la
     *         même requette
     */
    private List<DemandeEsDTO> findAllDemandesLazy() {
        String[] includes = new String[] { "identifiant", "pkDemandes" };
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
                .withSourceFilter(new FetchSourceFilter(includes, null))
                .withPageable(PageRequest.of(0, (int) demandeEsRepository.count())).build();
        return elasticsearchTemplate.search(searchQuery, DemandeEsDTO.class).stream().map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    @Override
    public DemandeDTO changerAffectationDemande(String demarcheId, int pkDemande, String agentAffecteId) {
        DemandeDTO demandeDTO = super.changerAffectationDemande(demarcheId, pkDemande, agentAffecteId);
        try {
            indexDemande(demandeDTO);
        } catch (Exception e) {
            LOGGER.error(SharedMessages.ERREUR_INDEXATION);
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent(
                    "IndexedEsDemandeServiceImpl - méthode changerAffectationDemande()", demandeDTO, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
        return demandeDTO;
    }

}

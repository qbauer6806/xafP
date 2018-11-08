package mc.gouv.af.back.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.simpleQueryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.join.query.JoinQueryBuilders.hasChildQuery;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
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

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.tika.exception.TikaException;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filters.FiltersAggregator.KeyedFilter;
import org.elasticsearch.search.aggregations.bucket.filters.InternalFilters;
import org.elasticsearch.search.aggregations.bucket.filters.InternalFilters.InternalBucket;
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
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;

import mc.gouv.af.back.config.es.IndexationEnabledCondition;
import mc.gouv.af.back.data.es.dao.DemandeEsRepository;
import mc.gouv.af.back.data.es.model.AgentEsDTO;
import mc.gouv.af.back.data.es.model.CanalEsDto;
import mc.gouv.af.back.data.es.model.DemandeAccessEsDTO;
import mc.gouv.af.back.data.es.model.DemandeEsDTO;
import mc.gouv.af.back.data.es.model.DemandeEsDTO.DemandeFileEsDTO;
import mc.gouv.af.back.data.es.model.DemandeEsJmsDto;
import mc.gouv.af.back.data.es.model.DemandeEsRechercheDTO;
import mc.gouv.af.back.data.es.model.DemandesFacet;
import mc.gouv.af.back.data.es.model.DemandesFacets;
import mc.gouv.af.back.data.es.model.EsProperty;
import mc.gouv.af.back.enumeration.JMSActionEnum;
import mc.gouv.af.back.exception.AfIndexingException;
import mc.gouv.af.back.exception.FileConnectionException;
import mc.gouv.af.back.service.DemandeJmsTopicSendService;
import mc.gouv.af.back.service.IndexedDemandeService;
import mc.gouv.af.back.service.transformer.DemandeEsTransformer;
import mc.gouv.af.back.util.FileUtils;
import mc.gouv.dem.data.dao.DemandesRepository;
import mc.gouv.dem.data.entity.DemandeBO;
import mc.gouv.dem.data.entity.DemandesComplementsBO;
import mc.gouv.dem.data.entity.DemandesFilesBO;
import mc.gouv.dem.service.AccessService;
import mc.gouv.dem.service.DemGouvPropertiesResolver;
import mc.gouv.dem.service.impl.DemandesServiceImpl;
import mc.gouv.dem.service.model.DemandeRechercheDTO;
import mc.gouv.dem.service.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.dem.service.transformer.DemandesFilesTransformer;
import mc.gouv.dem.service.util.DemarchesUtils;
import mc.gouv.dem.shared.model.DemandeCanalEnum;
import mc.gouv.dem.shared.model.DemandeComplementsDTO;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.DemandeFileDTO;
import mc.gouv.dem.shared.model.DemandeStatutDTO;
import mc.gouv.file.apiclient.FileClient;

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
    private DemGouvPropertiesResolver demGouvPropertiesResolver;

    @Inject
    private AccessService accessService;

    @Inject
    private DemandeEsTransformer demandeEsTransformer;

    @Value("${application.name}")
    private String indexAlias;

    @Value("${mc.gouv.stage.search.highlight.pretags:<b>}")
    private String highlightPretags;

    @Value("${mc.gouv.stage.search.highlight.posttags:</b>}")
    private String highlightPosttags;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    private List<EsProperty> demandesProperties = new ArrayList<>();
    private List<EsProperty> filesProperties = new ArrayList<>();

    private Map<String, String> propertiesFields = new HashMap<>();

    public static final String ES_KEYWORD = ".keyword";
    public static final SimpleDateFormat SDF = new SimpleDateFormat(DATE_PATTERN);
    public static final String ES_MAPPING_PROPERTIES_KEY = "properties";
    public static final String ES_MAPPING_FIELDS_KEY = "fields";
    public static final String ES_MAPPING_TYPE_KEY = "type";
    public static final String FILE_PJ_HIGHLIGHT_AND_FACET_PREFIX = "fichiers.";
    public static final String FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX = "fichiers.complement.";
    private List<String> facetsToExclude = Arrays.asList("complements.reponse.fichiers.name",
            "complements.reponse.fichiers.url", "fichiers.demandeId");

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexedEsDemandeServiceImpl.class);

    List<String> indices = new ArrayList<>();

    /**
     * Récupération des du mapping à partir d'un alias
     * @param aliasName Nom de l'alias
     * @param type Type de l'index
     * @return Mapping Elasticsearch
     */
    public Map getMapping(String aliasName, String type) {
        Assert.notNull(aliasName, "No index defined for putMapping()");
        Assert.notNull(type, "No type defined for putMapping()");
        Map mappings = null;
        try {

            if (indices.isEmpty()) {
                String[] indicesNames = elasticsearchTemplate.getClient().admin().indices()
                        .getIndex(new GetIndexRequest()).actionGet().getIndices();
                indices.addAll(Arrays.asList(indicesNames));
            }

            if (indices.isEmpty()) {
                throw new AfIndexingException("Problem retrieving index name");
            }

            mappings = elasticsearchTemplate.getClient().admin().indices()
                    .getMappings(new GetMappingsRequest().indices(aliasName).types(type)).actionGet().getMappings()
                    .get(indices.get(0)).get(type).getSourceAsMap();

        } catch (Exception e) {
            throw new ElasticsearchException("Error while getting mapping for indexName : " + aliasName + " type : "
                    + type + " " + e.getMessage());
        }
        return mappings;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public synchronized void initMappingProperties() {
        if (demandesProperties.isEmpty()) {
            Map<String, Map> mapping = getMapping(indexAlias, DemandeEsDTO.INDEX_TYPE);
            initMappingProperties(demandesProperties, mapping);
        }

        if (filesProperties.isEmpty()) {
            Map<String, Map> mapping = getMapping(indexAlias, DemandeEsDTO.INDEX_FILES_TYPE);
            initMappingProperties(filesProperties, mapping);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public synchronized void initMappingProperties(List<EsProperty> properties, Map<String, Map> mapping) {
        if (elasticsearchTemplate != null) {

            if (mapping != null) {
                for (Entry<String, Map> entry : mapping.entrySet()) {

                    if (entry.getKey().equals(ES_MAPPING_PROPERTIES_KEY)) {
                        Map<String, Map> map = entry.getValue();
                        for (Entry<String, Map> subMapentry : map.entrySet()) {
                            properties.add(new EsProperty(subMapentry.getKey()));
                            getPropertyName(subMapentry.getValue(), subMapentry.getKey(), properties);
                        }
                    } else {

                        Map<String, Map> mappingCheck = mapping.get(entry.getKey());
                        if (mappingCheck != null)
                            mapping = mappingCheck;
                        else
                            continue;
                    }

                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getPropertyName(Map<String, Map> map, String propertyName, List<EsProperty> peroperties) {

        if (map == null || map.isEmpty()) {
            return;
        }
        for (Entry<String, Map> entry : map.entrySet()) {
            if (entry.getKey().equals(ES_MAPPING_PROPERTIES_KEY)) {
                Map<String, Map> submap = entry.getValue();
                for (Entry<String, Map> subMapentry : submap.entrySet()) {
                    String newFiledName = propertyName + "." + subMapentry.getKey();
                    Integer filedIndex = peroperties.indexOf(new EsProperty(propertyName));
                    if (filedIndex < 0) {
                        peroperties.add(new EsProperty(newFiledName));
                    } else {
                        peroperties.set(filedIndex, new EsProperty(newFiledName));
                    }
                    getPropertyName(subMapentry.getValue(), newFiledName, peroperties);
                }
            } else if (entry.getKey().equals(ES_MAPPING_FIELDS_KEY)) {
                Map<String, Map> submap = entry.getValue();
                for (Entry<String, Map> subMapentry : submap.entrySet()) {

                    Integer filedIndex = peroperties.indexOf(new EsProperty(propertyName));
                    if (filedIndex >= 0) {
                        peroperties.get(filedIndex).addField(subMapentry.getKey());
                        propertiesFields.put(propertyName + "." + subMapentry.getKey(), propertyName);
                    }
                }

            }

            else if (entry.getKey().equals(ES_MAPPING_TYPE_KEY)) {
                String type = (String) ((Object) entry.getValue());
                Integer filedIndex = peroperties.indexOf(new EsProperty(propertyName));
                if (filedIndex >= 0) {
                    peroperties.get(filedIndex).setType(type);
                }

            }

        }

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
            List<DemandeFileEsDTO> pjs = new ArrayList<>();
            List<DemandeFileEsDTO> complementsFiles = new ArrayList<>();

            for (DemandeBO demande : demandes) {
                fillPjAndComplementsFilesList(pjs, complementsFiles, demande);
            }

            indexFiles(pjs);
            indexFiles(complementsFiles);
        }
    }

    /**
     * Méthode permettant l'indexation des fichiers d'une demande
     * 
     * @param demandes
     *            Liste des demandes dont on va indexer les fichiers
     * @throws IOException
     */
    @Override
    public void indexFiles(DemandeBO demande) throws IOException {

        if (demande != null) {
            List<DemandeFileEsDTO> pjs = new ArrayList<>();
            List<DemandeFileEsDTO> complementsFiles = new ArrayList<>();

            fillPjAndComplementsFilesList(pjs, complementsFiles, demande);

            indexFiles(pjs);
            indexFiles(complementsFiles);
        }
    }

    /**
     * Méthode permettant l'indexation des fichiers d'une demande
     * 
     * @param demandes
     *            Liste des demandes dont on va indexer les fichiers
     * @throws IOException
     */
    @Override
    public void indexFiles(DemandeDTO demande) throws IOException {

        if (demande != null) {
            List<DemandeFileEsDTO> pjs = new ArrayList<>();
            List<DemandeFileEsDTO> complementsFiles = new ArrayList<>();

            fillPjAndComplementsFilesList(pjs, complementsFiles, demande);

            indexFiles(pjs);
            indexFiles(complementsFiles);
        }
    }

    /**
     * Méthode permettant de récupérer la liste des pieces jointes et des complements au format elasticsearch
     * 
     * @param pjs
     *            Liste des pieces à remplir
     * @param complementsFiles
     *            Liste des complements à remplir
     * @param demande
     *            Demande concernée
     * @throws IOException
     */
    private void fillPjAndComplementsFilesList(List<DemandeFileEsDTO> pjs, List<DemandeFileEsDTO> complementsFiles,
            DemandeBO demande) throws IOException {

        List<DemandeFileDTO> demFiles = DemandesFilesTransformer
                .bo2Dto(new ArrayList<DemandesFilesBO>(demande.getFiles()));

        Set<DemandesComplementsBO> demComplements = demande.getDemandesComplements();

        if (demComplements != null) {
            for (DemandesComplementsBO demComplement : demComplements) {
                List<DemandeFileDTO> cfiles = DemandesComplementsFilesTransformer
                        .toDemandeFileDTO(demComplement.getFiles());
                if (cfiles != null && !cfiles.isEmpty()) {
                    complementsFiles.addAll(getFileEsContent(demande.getFkAccess().getDemarcheId(),
                            demande.getIdentifiant(), DemandeFileEsDTO.TYPE.COMPLEMENT, cfiles));
                }
            }
        }

        pjs.addAll(getFileEsContent(demande.getFkAccess().getDemarcheId(), demande.getIdentifiant(),
                DemandeFileEsDTO.TYPE.PIECE_JOINTE, demFiles));
    }

    /**
     * Méthode permettant de récupérer la liste des pieces jointes et des complements au format elasticsearch
     * 
     * @param pjs
     *            Liste des pieces à remplir
     * @param complementsFiles
     *            Liste des complements à remplir
     * @param demande
     *            Demande concernée
     * @throws IOException
     */
    private void fillPjAndComplementsFilesList(List<DemandeFileEsDTO> pjs, List<DemandeFileEsDTO> complementsFiles,
            DemandeDTO demande) throws IOException {

        DemandeComplementsDTO[] demComplements = demande.getComplements();

        if (demComplements != null) {
            for (DemandeComplementsDTO demComplement : demComplements) {
                if (demComplement.getReponse() != null && demComplement.getReponse().getFichiers() != null) {
                    List<DemandeFileDTO> cfiles = DemandesComplementsFilesTransformer
                            .toDemandeFileDTO(Arrays.asList(demComplement.getReponse().getFichiers()));
                    if (cfiles != null && !cfiles.isEmpty()) {
                        complementsFiles.addAll(getFileEsContent(demande.getDemarcheId(), demande.getIdentifiant(),
                                DemandeFileEsDTO.TYPE.COMPLEMENT, cfiles));
                    }
                }
            }
        }

        pjs.addAll(getFileEsContent(demande.getDemarcheId(), demande.getIdentifiant(),
                DemandeFileEsDTO.TYPE.PIECE_JOINTE, Arrays.asList(demande.getFichiers())));
    }

    /**
     * @see mc.gouv.dem.service.DemandesService#reindex()
     */
    @Override
    public Long reindex() throws IOException, SAXException, TikaException {

        if (demandeEsRepository != null) {
            long demCount = demandesRepository.count();
            demandeEsRepository.deleteAll();
            final int size = demGouvPropertiesResolver.getEsReindexBulkSize();
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

            return demCount;
        }
        return 0l;
    }

    @Override
    public void indexDemande(DemandeDTO demandeDTO) throws IOException, SAXException, TikaException, JMSException {

        Boolean activeAccess = accessService.isAccessActive(demandeDTO.getFkAccess());
        DemandeEsDTO demandeEsDTO = demandeEsTransformer.toEs(demandeDTO, activeAccess);
        demandeJmsService.send(new DemandeEsJmsDto(demandeEsDTO, null), JMSActionEnum.SAVE);

    }

    @Override
    public void sendToTopic(DemandeDTO demandeDTO) throws IOException, SAXException, TikaException, JMSException {

        if (demandeDTO != null) {

            Boolean activeAccess = accessService.isAccessActive(demandeDTO.getFkAccess());
            DemandeEsDTO demandeEsDTO = demandeEsTransformer.toEs(demandeDTO, activeAccess);

            List<DemandeFileEsDTO> files = new ArrayList<>();

            List<DemandeFileEsDTO> pjs = new ArrayList<>();
            List<DemandeFileEsDTO> complementsFiles = new ArrayList<>();
            fillPjAndComplementsFilesList(pjs, complementsFiles, demandeDTO);

            files.addAll(pjs);
            files.addAll(complementsFiles);

            demandeJmsService.send(new DemandeEsJmsDto(demandeEsDTO, files), JMSActionEnum.SAVE);
        }

    }

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
            FileClient fileClient = new FileClient(DemarchesUtils.FILE_REST_URL, DemarchesUtils.FILE_JWT);
            InputStream is;
            try {
                is = fileClient.getFile(
                        demarcheId + "/" + DemarchesUtils.CONTAINERID + "/" + fichier.getUrl().replace(" ", "%20"));
            } catch (ConnectException e) {
                throw new FileConnectionException("Could not connect to file", e);
            }
            DemandeFileEsDTO demandeFileEsDTO = new DemandeFileEsDTO();
            demandeFileEsDTO.setMeta(fichier.getMeta());
            demandeFileEsDTO.setName(fichier.getName());
            demandeFileEsDTO.setUrl(fichier.getUrl());
            demandeFileEsDTO.setDemandeId(demIdentifiant);
            demandeFileEsDTO.setType(type.name());

            if (is != null) {
                String fileText = "";
                try {
                    fileText = FileUtils.parseToPlainText(is);
                    demandeFileEsDTO.setContent(fileText);
                    demandeFileEsDTO.setLanguage(FileUtils.detectLanguage(fileText));
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
     * @param type
     *            Type du fichier à indexer
     * @return Fichier indexé
     */
    private DemandeFileEsDTO indexFile(DemandeFileEsDTO demandeFileEsDTO) {

        if (demandeFileEsDTO != null) {
            IndexQuery index = new IndexQuery();
            index.setId(demandeFileEsDTO.getId());
            index.setObject(demandeFileEsDTO);
            index.setParentId(demandeFileEsDTO.getDemandeId());
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
                index.setId(demFile.getId());
                index.setObject(demFile);
                index.setParentId(demFile.getDemandeId());
                indexList.add(index);
            }

            if (!indexList.isEmpty()) {
                elasticsearchTemplate.bulkIndex(indexList);
                elasticsearchTemplate.refresh(DemandeFileEsDTO.class);
            }

        }
        return demandeFileEsDTOs;
    }

    @Override
    public List<DemandeEsDTO> getIndexedDemandes(DemandeRechercheDTO demandeRecherche) {

        return Lists.newArrayList(demandeEsRepository.search(getQueryBuilder(demandeRecherche)));
    }

    /**
     * @see mc.gouv.dem.service.DemandesService#getDemandesFacets(mc.gouv.dem.service.model.DemandeRechercheDTO)
     */
    @Override
    public DemandesFacets getDemandesFacets(DemandeRechercheDTO demandeRecherche) {

        initMappingProperties();

        if (!StringUtils.isBlank(demandeRecherche.getTexte())) {

            NativeSearchQueryBuilder nativeSearchQueryBuilder = getFacetsAggregationQuery(demandeRecherche);

            return elasticsearchTemplate.query(nativeSearchQueryBuilder.build(),
                    new ResultsExtractor<DemandesFacets>() {

                        @Override
                        public DemandesFacets extract(SearchResponse response) {

                            DemandesFacets facets = new DemandesFacets();

                            if (response.getAggregations().asList().isEmpty()) {
                                return null;
                            }

                            for (Aggregation agg : response.getAggregations().asList()) {
                                InternalFilters filters = (InternalFilters) agg;
                                for (InternalBucket bucket : filters.getBuckets()) {
                                    if (bucket.getDocCount() > 0
                                            && !facetsToExclude.contains(bucket.getKeyAsString())) {
                                        facets.add(new DemandesFacet(bucket.getKeyAsString(), bucket.getDocCount()));
                                    }

                                }

                            }
                            return facets;
                        }
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
                + filesProperties.size() * 2];

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
                    SimpleQueryStringBuilder sqsb = simpleQueryStringQuery(text).defaultOperator(Operator.OR)
                            .fields(fields).lenient(true);
                    TermQueryBuilder pjtqb = termQuery(DemandeFileEsDTO.TYPE_FIELD,
                            DemandeFileEsDTO.TYPE.PIECE_JOINTE.name());
                    BoolQueryBuilder pjbqb = boolQuery().must(sqsb).must(pjtqb);
                    HasChildQueryBuilder pjHasChildQueryBuilder = hasChildQuery(DemandeEsDTO.INDEX_FILES_TYPE, pjbqb,
                            ScoreMode.Avg);

                    queryStringQueryBuilders[index] = new KeyedFilter(
                            FILE_PJ_HIGHLIGHT_AND_FACET_PREFIX + property.getName(), pjHasChildQueryBuilder);

                    index++;

                    TermQueryBuilder comptqb = termQuery(DemandeFileEsDTO.TYPE_FIELD,
                            DemandeFileEsDTO.TYPE.COMPLEMENT.name());

                    BoolQueryBuilder compbqb = boolQuery().must(sqsb).must(comptqb);

                    HasChildQueryBuilder compHasChildQueryBuilder = hasChildQuery(DemandeEsDTO.INDEX_FILES_TYPE,
                            compbqb, ScoreMode.Avg);

                    queryStringQueryBuilders[index] = new KeyedFilter(
                            FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX + property.getName(), compHasChildQueryBuilder);

                } else {
                    queryStringQueryBuilders[index] = new KeyedFilter(property.getName(),
                            simpleQueryStringQuery(text).defaultOperator(Operator.OR).fields(fields).lenient(true));
                }

                index++;
            }

        }
        return index;
    }

    @Override
    public Page<DemandeEsRechercheDTO> getIndexedDemandes(DemandeRechercheDTO demandeRecherche, Pageable pageable,
            String[] fields) {

        initMappingProperties();

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
                            updateHighLightedField(highlightFields, demEsHighlightFields, false, false);

                            Map<String, SearchHits> innerHits = searchHit.getInnerHits();

                            if (innerHits != null) {
                                for (Entry<String, SearchHits> searchHitsEntry : innerHits.entrySet()) {
                                    SearchHit[] searchHitsArray = searchHitsEntry.getValue().getHits();
                                    for (SearchHit searchInnerHit : searchHitsArray) {

                                        SearchHitField typeField = searchInnerHit.getField(DemandeFileEsDTO.TYPE_FIELD);
                                        String type = typeField.getValue();

                                        boolean isPj = false;
                                        boolean isComplement = false;

                                        if (type.equals(DemandeFileEsDTO.TYPE.PIECE_JOINTE.name())) {
                                            isPj = true;
                                            isComplement = false;
                                        } else if (type.equals(DemandeFileEsDTO.TYPE.COMPLEMENT.name())) {
                                            isPj = false;
                                            isComplement = true;
                                        }

                                        updateHighLightedField(searchInnerHit.getHighlightFields(),
                                                demEsHighlightFields, isPj, isComplement);
                                    }
                                }
                            }

                            demandeEsRechercheDTO.setHighlightedField(demEsHighlightFields);
                            demandesEsList.add(demandeEsRechercheDTO);

                        }

                        return new AggregatedPageImpl<>((List<T>) demandesEsList, pageable,
                                response.getHits().getTotalHits());
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
     * @param isPj
     *            Boolean pour indiquer si on recherche dans les champs d'un fichier de type piece jointe
     * @param isComplement
     *            Boolean pour indiquer si on recherche dans les champs d'un fichier de type complement
     */
    private void updateHighLightedField(Map<String, HighlightField> highlightFields,
            Map<String, String> demEsHighlightFields, boolean isPj, boolean isComplement) {
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

                if (isPj) {
                    fragmentField = fragmentField.insert(0, FILE_PJ_HIGHLIGHT_AND_FACET_PREFIX);
                } else if (isComplement) {
                    fragmentField = fragmentField.insert(0, FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX);
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

            if (!StringUtils.isBlank(demandeRecherche.getSearchField())) {
                List<String> demandeSearchFields = getSearchFields(demandeRecherche.getSearchField(),
                        demandesProperties);
                HighlightBuilder.Field[] fieldsForHighlight = new HighlightBuilder.Field[demandeSearchFields.size()];
                int i = 0;

                if (!demandeSearchFields.isEmpty()) {
                    for (String serachField : demandeSearchFields) {
                        fieldsForHighlight[i] = new HighlightBuilder.Field(serachField).preTags(highlightPretags)
                                .postTags(highlightPosttags)
                                .highlightQuery(simpleQueryStringQuery(demandeRecherche.getTexte())
                                        .defaultOperator(Operator.OR).useAllFields(true));
                        i++;
                    }
                }

                return nativeSearchQueryBuilder.withHighlightFields(fieldsForHighlight);
            } else {
                return nativeSearchQueryBuilder.withHighlightFields(
                        new HighlightBuilder.Field("*").preTags(highlightPretags).postTags(highlightPosttags)
                                .highlightQuery(simpleQueryStringQuery(demandeRecherche.getTexte())
                                        .defaultOperator(Operator.OR).useAllFields(true)));
            }

        }
        return nativeSearchQueryBuilder;
    }

    /**
     * Méthode permettant de récupérer les fields sur lesquels on va faire la recherche à partir d'une propriété
     * 
     * @param propertyName
     *            Nom de la propriétés
     * @param liste
     *            des propriétés elasticsearch
     * @return Liste des fields sur lesquels on va faire la recherche
     */
    private List<String> getSearchFields(String propertyName, List<EsProperty> properties) {
        List<String> serachFields = new ArrayList<>();
        if (properties.contains(new EsProperty(propertyName))) {
            EsProperty property = properties.get(properties.indexOf(new EsProperty(propertyName)));
            serachFields.add(propertyName);
            property.getFields().stream().forEach(field -> serachFields.add(propertyName + "." + field));
        }

        return serachFields;
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

            SimpleQueryStringBuilder demandeQueryStringQueryBuilder = simpleQueryStringQuery(
                    demandeRecherche.getTexte()).defaultOperator(Operator.OR);
            SimpleQueryStringBuilder filesQueryStringQueryBuilder = simpleQueryStringQuery(demandeRecherche.getTexte())
                    .defaultOperator(Operator.OR);

            if (!StringUtils.isBlank(demandeRecherche.getSearchField())) {
                boolQueryBuilder = getQueryWhereFacetClicked(demandeQueryStringQueryBuilder,
                        filesQueryStringQueryBuilder, demandeRecherche.getTexte(), demandeRecherche.getSearchField(),
                        boolQueryBuilder);

            } else {
                boolQueryBuilder = getQueryWhereFacetNotClicked(demandeQueryStringQueryBuilder,
                        filesQueryStringQueryBuilder, demandeRecherche.getTexte(), boolQueryBuilder);
            }

        }

        return getUiFilterQuery(boolQueryBuilder, demandeRecherche);

    }

    /**
     * Méthode permattant la construction de la requete de recupération des demandes lorsque on a cliqué sur aucune
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
        demandeQueryStringQueryBuilder = demandeQueryStringQueryBuilder.useAllFields(true);
        filesQueryStringQueryBuilder = filesQueryStringQueryBuilder.useAllFields(true);
        HighlightBuilder.Field field = new HighlightBuilder.Field("*").preTags(highlightPretags)
                .postTags(highlightPosttags)
                .highlightQuery(simpleQueryStringQuery(rechercheText).defaultOperator(Operator.OR).useAllFields(true));
        HighlightBuilder hb = new HighlightBuilder().field(field);
        InnerHitBuilder ihb = new InnerHitBuilder().setHighlightBuilder(hb)
                .setStoredFieldNames(Arrays.asList(DemandeFileEsDTO.TYPE_FIELD));
        HasChildQueryBuilder hasChildQueryBuilder = hasChildQuery(DemandeEsDTO.INDEX_FILES_TYPE,
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
     * @param searchField
     *            facet sur lequel on a cliqué
     * @param boolQueryBuilder
     *            Requete globale qui combine les requetes sur les demandes et sur les fichiers
     * @return Requete globale qui combine les requetes sur les demandes et sur les fichiers
     */
    private BoolQueryBuilder getQueryWhereFacetClicked(SimpleQueryStringBuilder demandeQueryStringQueryBuilder,
            SimpleQueryStringBuilder filesQueryStringQueryBuilder, String rechercheText, String searchField,
            BoolQueryBuilder boolQueryBuilder) {
        TermQueryBuilder tqb = null;

        if (searchField != null && searchField.startsWith(FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX)) {
            searchField = searchField.replaceFirst(FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX, "");
            tqb = termQuery(DemandeFileEsDTO.TYPE_FIELD, DemandeFileEsDTO.TYPE.COMPLEMENT.name());
            boolQueryBuilder.must(hasChildQuery(DemandeEsDTO.INDEX_FILES_TYPE, tqb, ScoreMode.Avg));
        } else if (searchField != null && searchField.startsWith(FILE_PJ_HIGHLIGHT_AND_FACET_PREFIX)) {
            searchField = searchField.replaceFirst(FILE_PJ_HIGHLIGHT_AND_FACET_PREFIX, "");
            tqb = termQuery(DemandeFileEsDTO.TYPE_FIELD, DemandeFileEsDTO.TYPE.PIECE_JOINTE.name());
            boolQueryBuilder.must(hasChildQuery(DemandeEsDTO.INDEX_FILES_TYPE, tqb, ScoreMode.Avg));
        }

        boolQueryBuilder = boolQueryBuilder.minimumShouldMatch(1);

        List<String> serachDemandeFields = getSearchFields(searchField, demandesProperties);

        List<String> serachFilesFields = getSearchFields(searchField, filesProperties);

        if (!serachDemandeFields.isEmpty()) {
            Map<String, Float> demandeFields = new HashMap<>();
            serachDemandeFields.stream().forEach(f -> demandeFields.put(f, 1f));
            demandeQueryStringQueryBuilder = demandeQueryStringQueryBuilder.fields(demandeFields);
            boolQueryBuilder = boolQueryBuilder.should(demandeQueryStringQueryBuilder);
        }

        if (!serachFilesFields.isEmpty()) {

            Map<String, Float> filesFields = new HashMap<>();
            HighlightBuilder hb = new HighlightBuilder();
            for (String f : serachFilesFields) {
                filesFields.put(f, 1f);
                HighlightBuilder.Field field = new HighlightBuilder.Field(f).preTags(highlightPretags)
                        .postTags(highlightPosttags).highlightQuery(
                                simpleQueryStringQuery(rechercheText).defaultOperator(Operator.OR).useAllFields(true));
                hb = hb.field(field);

            }
            InnerHitBuilder ihb = new InnerHitBuilder().setHighlightBuilder(hb)
                    .setStoredFieldNames(Arrays.asList(DemandeFileEsDTO.TYPE_FIELD));
            filesQueryStringQueryBuilder = filesQueryStringQueryBuilder.fields(filesFields);
            if (tqb != null) {
                BoolQueryBuilder bqb = boolQuery().must(filesQueryStringQueryBuilder).must(tqb);
                HasChildQueryBuilder hasChildQueryBuilder = hasChildQuery(DemandeEsDTO.INDEX_FILES_TYPE, bqb,
                        ScoreMode.Avg).innerHit(ihb);
                boolQueryBuilder = boolQueryBuilder.should(hasChildQueryBuilder);
            } else {
                HasChildQueryBuilder hasChildQueryBuilder = hasChildQuery(DemandeEsDTO.INDEX_FILES_TYPE,
                        filesQueryStringQueryBuilder, ScoreMode.Avg).innerHit(ihb);
                boolQueryBuilder = boolQueryBuilder.should(hasChildQueryBuilder);
            }

        }
        return boolQueryBuilder;
    }

    /**
     * Méthode permettant la construction de la requete elasticsearch à partir des filtres de la recherche avancée
     * 
     * @param boolQueryBuilder
     *            Requete globale qui combine les requetes sur les demandes, sur les fichiers et sur les filtres
     *            définits dans l'interface graphique
     * @param demandeRecherche
     *            DTO contenant les champs de la recherche (filtres+barre de recherche)
     * @return Requete globale qui combine les requetes sur les demandes, sur les fichiers et sur les filtres définits
     *         dans l'interface graphique
     */
    private BoolQueryBuilder getUiFilterQuery(BoolQueryBuilder boolQueryBuilder, DemandeRechercheDTO demandeRecherche) {
        if (demandeRecherche.getStatuts() != null) {
            boolQueryBuilder = boolQueryBuilder.must(termsQuery(
                    DemandeEsDTO.DERNIER_STATUT_FIELD_NAME + "." + DemandeStatutDTO.LIBELLE_FIELD_NAME + ES_KEYWORD,
                    demandeRecherche.getStatuts()));
        }

        if (demandeRecherche.getCanaux() != null) {
            boolQueryBuilder = boolQueryBuilder.must(termsQuery(
                    DemandeEsDTO.CANAL_FIELD_NAME + "." + CanalEsDto.CANAL_CODE_FIELD_NAME + ES_KEYWORD,
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

        return boolQueryBuilder;

    }

    /**
     * {@inheritDoc}
     * 
     * @throws SAXException
     * @throws IOException
     */
    @Override
    public DemandeDTO saveDemande(DemandeDTO demande, String premierStatut) throws IOException, SAXException {

        DemandeDTO demandeDto = super.saveDemande(demande, premierStatut);
        try {
            sendToTopic(demandeDto);
        } catch (TikaException | JMSException e) {
            LOGGER.error(e.getMessage(), e);
            throw new AfIndexingException(e.getMessage(), e);
        }
        return demandeDto;
    }

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
     * {@inheritDoc}
     * 
     * @throws JMSException
     * @throws JsonProcessingException
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

    public String getFormatedDate(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        return SDF.format(cal.getTime());
    }

}

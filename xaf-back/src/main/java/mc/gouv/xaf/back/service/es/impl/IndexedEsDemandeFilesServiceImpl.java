package mc.gouv.xaf.back.service.es.impl;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsBO;
import mc.gouv.xaf.back.data.entity.DemandesCourriersBO;
import mc.gouv.xaf.back.data.es.model.DemandeFileEsDTO;
import mc.gouv.xaf.back.data.es.model.EsErrorEventDTO;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesCourriersTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesFilesTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.exception.AfIndexingException;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.impl.DemandeFilesServiceImpl;
import mc.gouv.xaf.back.service.es.IndexedFilesService;
import mc.gouv.xaf.back.service.es.handlers.EsTransactionErrorsHandler;
import mc.gouv.xaf.back.service.es.transformer.DemandeCourrierFilesTransformer;
import mc.gouv.xaf.back.service.es.transformer.DemandeFileEsTransformer;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.glassfish.jersey.internal.guava.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

@Service
@Primary
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackFor = Exception.class)
public class IndexedEsDemandeFilesServiceImpl extends DemandeFilesServiceImpl implements IndexedFilesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexedEsDemandeFilesServiceImpl.class);

    private static final int MAX_BULK_SIZE = 10;

    @Value("${application.name}")
    private String indexAlias;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private DemandeFileEsTransformer demandeFileEsTransformer;

    @Inject
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Inject
    private ElasticsearchConverter elasticsearchConverter;

    @Override
    public void saveFile(DemandeFileDTO demandeFile, String demarcheId, Integer pkDemande) throws Exception {
        super.saveFile(demandeFile, demarcheId, pkDemande);
        DemandeBO demandeBo = demandesService.getDemandeBo(demarcheId, pkDemande);
        DemandeDTO demandeDTO = DemandesTransformer.bo2Dto(demandeBo);
        // Indexation
        try {
            DemandeFileEsDTO demandeFileEsDTO = demandeFileEsTransformer.getFileEsContent(demandeDTO, FileUtils.getDemandeFileType(demandeFile), demandeFile);
            List<DemandeFileEsDTO> demFileEsDtoList = new ArrayList<>();
            demFileEsDtoList.add(demandeFileEsDTO);
            LOGGER.info("Appel de la méthode indexFiles");
            indexFiles(demFileEsDtoList);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'indexation du fichier.");
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedEsDemandeFilesServiceImpl - méthode saveFile()", demandeDTO, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
    }

    @Override
    public void indexElement(DemandeFileDTO demandeFileDTO, DemandeDTO demandeDTO) throws IOException {

        if (demandeFileDTO != null) {

            DemandeFileEsDTO demandeFileEsDTO = demandeFileEsTransformer.getFileEsContent(demandeDTO, FileUtils.getDemandeFileType(demandeFileDTO), demandeFileDTO);
            List<DemandeFileEsDTO> demFileEsDtoList = new ArrayList<>();
            demFileEsDtoList.add(demandeFileEsDTO);

            LOGGER.info("Appel de la méthode indexFiles");
            indexFiles(demFileEsDtoList);
        }

        LOGGER.info("Fin de l'indexation des fichiers");
    }

    @Override
    public void indexElement(DemandeFileDTO[] demandeFileDTOList, DemandeDTO demandeDTO)
            throws IOException {

        if (demandeFileDTOList != null) {

            List<DemandeFileEsDTO> demFileEsDtoList = new ArrayList<>();
            for (DemandeFileDTO file : demandeFileDTOList) {
                demFileEsDtoList.add(demandeFileEsTransformer.getFileEsContent(demandeDTO, FileUtils.getDemandeFileType(file), file));
            }

            LOGGER.info("Appel de la méthode indexFiles.");
            indexFiles(demFileEsDtoList);
        }

        LOGGER.info("Fin de l'indexation des fichiers.");
    }

    /**
     * Permet d'indexer les fichiers d'une demande de manière asynchrone
     */
    @Async
    @Override
    public void indexFilesAsynchrone(DemandeDTO demandeDTO) {
        List<DemandeFileEsDTO> files = new ArrayList<>();
        try {
            fillFilesList(files, demandeDTO);
        } catch (IOException e) {
            LOGGER.error("Indexation des fichiers Asynchrone - Problème lors du parsing des fichiers", e);
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedEsDemandeFilesServiceImpl - méthode indexFilesAsynchrone() - parsing des fichiers", demandeDTO, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }

        if (files.isEmpty()) {
            LOGGER.info("Aucun fichiers à indexer");
        } else {
            LOGGER.info("Il y a {} fichier(s) à indexer.", files.size());
            try {
                indexFiles(files);
            } catch (IOException e) {
                LOGGER.error("Indexation des fichiers Asynchrone - Problème lors de l'indexation des fichiers", e);
                EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedEsDemandeFilesServiceImpl - méthode indexFilesAsynchrone() - indexation des fichiers", demandeDTO, e);
                applicationEventPublisher.publishEvent(esErrorEventDTO);
                throw new AfIndexingException(e.getMessage(), e);
            }
        }
        LOGGER.info("Fin de l'indexation des fichiers");
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
     * Méthode permettant l'indexation des fichiers des demandes
     *
     * @param demandes Liste des demandes dont on va indexer les fichiers
     */
    @Override
    public void indexFiles(Page<DemandeBO> demandes) throws IOException {
        if (demandes != null) {
            List<DemandeFileEsDTO> files = new ArrayList<>();
            for (DemandeBO demande : demandes) {
                fillFilesList(files, demande);
            }
            indexFiles(files);
        }
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
                index.setId(demFile.getPkDemandes() + "-" + demFile.getIdentifiant());
                index.setObject(demFile);
                index.setParentId(demFile.getDemandeJoinField().getParent());
                indexList.add(index);
            }

            if (!indexList.isEmpty()) {
                bulkIndex(indexList);
                // TODO elasticsearchTemplate.refresh(DemandeFileEsDTO.class);
            }

        }
        return demandeFileEsDTOs;
    }

    private void bulkIndex(List<IndexQuery> queries) {
        List<IndexQuery> bulkQueries = new ArrayList<>();
        int nombreBulks = (queries.size() + MAX_BULK_SIZE - 1) / MAX_BULK_SIZE;
        LOGGER.info("Début indexation pour {} fichiers en {} requêtes", queries.size(), nombreBulks);

        for (int i = 0; i < queries.size(); i++) {
            // Envois et Création d'une nouvelle bulk request si on arrive au max bulk size
            if (i != 0 && i % MAX_BULK_SIZE == 0) {
                LOGGER.info("Indexation du bulk {}/{}", i / MAX_BULK_SIZE, nombreBulks);
                // TODO checkForBulkUpdateFailure(elasticsearchTemplate.getClient().bulk(bulkRequest, RequestOptions.DEFAULT));
                elasticsearchTemplate.bulkIndex(bulkQueries, IndexCoordinates.of(indexAlias));
                bulkQueries.clear();
            }
            //bulkRequest.add(prepareIndex(queries.get(i)));
            IndexQuery query = queries.get(i);
            query.setRouting(query.getParentId());
            bulkQueries.add(query);
        }

        LOGGER.info("Indexation du bulk {}/{}", nombreBulks, nombreBulks);
        // TODO checkForBulkUpdateFailure(elasticsearchTemplate.getClient().bulk(bulkRequest, RequestOptions.DEFAULT));
        elasticsearchTemplate.bulkIndex(bulkQueries, IndexCoordinates.of(indexAlias));
    }

    private IndexRequest prepareIndex(IndexQuery query) {
        IndexRequest indexRequest;

        if (query.getObject() != null) {
            // If we have a query id and a document id, do not ask ES to generate one.
            indexRequest = new IndexRequest(indexAlias).id(query.getId());
            indexRequest.source(elasticsearchConverter.mapObject(query.getObject()));
        } else {
            throw new ElasticsearchException("object or source is null, failed to index the document [id: " + query.getId() + "]");
        }

        indexRequest.routing(query.getParentId());
        return indexRequest;
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

    /**
     * Méthode permettant de récupérer la liste des pieces jointes, des complements et courriers au format elasticsearch
     *
     * @param files   Liste des fichiers à remplir
     * @param demande Demande concernée
     * @throws IOException
     */
    @Override
    public void fillFilesList(List<DemandeFileEsDTO> files, DemandeBO demande) throws IOException {

        List<DemandeFileDTO> demFiles = DemandesFilesTransformer.bo2Dto(new ArrayList<>(demande.getFiles()));

        demFiles.addAll(DemandeCourrierFilesTransformer.recupererCourriersDemandeFromBO(demande.getCourriers()));

        Set<DemandesComplementsBO> demComplements = demande.getDemandesComplements();
        DemandeDTO demandeDTO = DemandesTransformer.bo2Dto(demande);

        if (demComplements != null) {
            for (DemandesComplementsBO demComplement : demComplements) {
                List<DemandeFileDTO> cfiles = DemandesComplementsFilesTransformer.toDemandeFileDTO(demComplement.getFiles());
                if (!cfiles.isEmpty()) {
                    files.addAll(demandeFileEsTransformer.getListFileEsContent(demandeDTO, DemandeFileEsDTO.TYPE.COMPLEMENT, cfiles));
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
    @Override
    public void fillFilesList(List<DemandeFileEsDTO> files, DemandeDTO demande) throws IOException {

        DemandeComplementsDTO[] demComplements = demande.getComplements();

        if (demComplements != null) {
            for (DemandeComplementsDTO demComplement : demComplements) {
                if (demComplement.getReponse() != null && demComplement.getReponse().getFichiers() != null) {
                    List<DemandeFileDTO> cfiles = DemandesComplementsFilesTransformer
                            .toDemandeFileDTO(Arrays.asList(demComplement.getReponse().getFichiers()));
                    if (!cfiles.isEmpty()) {
                        files.addAll(demandeFileEsTransformer.getListFileEsContent(demande, DemandeFileEsDTO.TYPE.COMPLEMENT, cfiles));
                    }
                }
            }
        }

        List<DemandeFileDTO> fichiers = new ArrayList<>();
        if (demande.getFichiers() != null) {
            fichiers.addAll(Arrays.asList(demande.getFichiers()));
        }

        if (demande.getCourriers() != null) {
            fichiers.addAll(DemandeCourrierFilesTransformer.recupererCourriersDemandeFromDTO(Arrays.asList(demande.getCourriers())));
        }

        fillPjsAndFichiersInternesAndCourriers(fichiers, files, demande);
        if (demande.getCourriers() != null) {
            fillCourriers(Arrays.asList(demande.getCourriers()), files, demande);
        }
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
                files.add(demandeFileEsTransformer.getFileEsContent(demandeDTO, FileUtils.getDemandeFileType(file), file));
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
                files.add(demandeFileEsTransformer.getFileEsContent(demandeDTO, DemandeFileEsDTO.TYPE.COURRIER, courrier));
            }
        }
    }
}

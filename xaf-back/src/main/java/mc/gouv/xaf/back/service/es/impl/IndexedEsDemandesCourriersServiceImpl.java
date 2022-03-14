package mc.gouv.xaf.back.service.es.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import mc.gouv.xaf.back.data.es.dao.DemandesFilesEsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.es.model.DemandeFileEsDTO;
import mc.gouv.xaf.back.data.es.model.EsErrorEventDTO;
import mc.gouv.xaf.back.exception.AfIndexingException;
import mc.gouv.xaf.back.service.data.impl.DemandesCourriersServiceImpl;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.es.IndexedFilesService;
import mc.gouv.xaf.back.service.es.handlers.EsTransactionErrorsHandler;
import mc.gouv.xaf.back.service.es.transformer.DemandeCourrierFilesTransformer;
import mc.gouv.xaf.back.service.es.transformer.DemandeFileEsTransformer;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

@Service
@Primary
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackFor = Exception.class)
public class IndexedEsDemandesCourriersServiceImpl extends DemandesCourriersServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexedEsDemandesCourriersServiceImpl.class);

    @Inject
    IndexedDemandeService indexedDemandeService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    
    @Autowired
    private DemandeFileEsTransformer demandeFileEsTransformer;
    
    @Autowired
    private IndexedFilesService indexedFilesService;

    @Inject
    private DemandesFilesEsRepository demandesFilesEsRepository;

    @Override
    public DemandeCourrierDTO saveCourrier(String demarcheId, Integer pkDemande, DemandeCourrierDTO courrierDto)
            throws Exception {

        DemandeCourrierDTO demandeCourrierDTO = super.saveCourrier(demarcheId, pkDemande, courrierDto);
        indexCourrier(demarcheId, pkDemande);
        return demandeCourrierDTO;
    }

    @Override
    public DemandeCourrierDTO updateCourrier(String demarcheId, Integer pkDemande, DemandeCourrierDTO courrierDto)
            throws Exception {
        DemandeCourrierDTO demandeCourrierDTO = super.updateCourrier(demarcheId, pkDemande, courrierDto);
        indexCourrier(demarcheId, pkDemande);
        return demandeCourrierDTO;
    }

    private void indexCourrier(String demarcheId, Integer pkDemande) {
        DemandeDTO demandeDTO = indexedDemandeService.getDemande(demarcheId, pkDemande);
        // Indexation
        try {
        	List<DemandeFileDTO> courriers = DemandeCourrierFilesTransformer.recupererCourriersDemandeFromDTO(Arrays.asList(demandeDTO.getCourriers()));
        	List<DemandeFileEsDTO> files = new ArrayList<>();
            if (!courriers.isEmpty()) {
                for (DemandeCourrierDTO courrier : demandeDTO.getCourriers()) {
                    files.add(demandeFileEsTransformer.getFileEsContent(demandeDTO, DemandeFileEsDTO.TYPE.COURRIER, courrier));
                }
                indexedFilesService.indexFiles(files);
            }
            indexedDemandeService.indexElement(demandeDTO, false);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'indexation du courrier.");
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedEsDemandesCourriersServiceImpl - méthode indexCourrier()", demandeDTO, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
    }
    
    /**
     * Méthode permettant de supprimer une demande et de la supprimer de l'index elasticsearch
     *
     * @see mc.gouv.xaf.back.service.data.impl.DemandesServiceImpl#deleteDemande(java.lang.String, java.lang.Integer)
     */
    @Override
    public void deleteCourriers(String demarcheId, Integer demandeId) {
    	LOGGER.info("Début de suppression des références des courriers dans Elasticsearch...");
        try {
            List<DemandeCourrierDTO> courriersToDelete = getCourriers(demarcheId, demandeId);
    		if(null != courriersToDelete && !courriersToDelete.isEmpty()) {
                List<String> idsToDelete = new ArrayList<>();
    			for (DemandeCourrierDTO currentCourriersToDelete : courriersToDelete) {
                    // L'identifiant ES est formé à partir de l'url du fichier
    				String identifiantCourrierStr = currentCourriersToDelete.getUrl().replace("/", "-");
                    // Ici le format de l'ID d'un courrier dans ES est {pkDemande}-{identifiant}
    				String currentCourrierEsId = demandeId + "-" + identifiantCourrierStr;
    				// On ajoute à la liste d'ids à supprimer
                    idsToDelete.add(currentCourrierEsId);
                }
                // Puis on appel le repo pour supprimer les fichiers
                LOGGER.info("Début suppression des courriers : {} dans ElasticSearch", idsToDelete);
                demandesFilesEsRepository.deleteAllById(idsToDelete);
                LOGGER.info("Fin suppression des courriers : {} dans ElasticSearch", idsToDelete);
    		}
    		super.deleteCourriers(demarcheId, demandeId);
        } catch (Exception e) {
            LOGGER.error("Erreur d'indexation lors de la suppression de courriers de la demande");
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedEsDemandesCourriersServiceImpl - méthode deleteCourriers()", demarcheId, demandeId, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
    }

    
}

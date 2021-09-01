package mc.gouv.xaf.back.service.es.impl;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.es.model.DemandeFileEsDTO;
import mc.gouv.xaf.back.data.es.model.EsErrorEventDTO;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.back.exception.AfIndexingException;
import mc.gouv.xaf.back.service.data.impl.DemandesComplementsServiceImpl;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.es.handlers.EsTransactionErrorsHandler;
import mc.gouv.xaf.back.service.es.transformer.DemandeFileEsTransformer;
import mc.gouv.xaf.shared.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Primary
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackFor = Exception.class)
public class IndexedDemandesComplementsServiceImpl extends DemandesComplementsServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexedDemandesComplementsServiceImpl.class);

    @Inject
    IndexedDemandeService indexedDemandeService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private DemandeFileEsTransformer demandeFileEsTransformer;

    @Override
    @Transactional
    public DemandeComplementsDTO saveDemandeComplements(String demarcheId, Integer demandeId,
                                                        DemandeComplementsQuestionDTO demandeComplements) throws Exception {
        // Sauvegarde en BDD
        DemandeComplementsDTO demandeComplementsDTO = super.saveDemandeComplements(demarcheId, demandeId,
                demandeComplements);

        // Indexation
        try {
            indexedDemandeService.indexDemande(demarcheId, demandeId);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'indexation du complément de la demande.");
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedDemandesComplementsServiceImpl - méthode saveDemandeComplements()", demarcheId, demandeId, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }

        return demandeComplementsDTO;
    }

    @Override
    public DemandeComplementsDTO repondreDemandeComplements(String demarcheId, Integer pkDemande,
                                                            Integer pkDemandeComplements, DemandeComplementsReponseDTO demandeComplementsReponse) throws Exception {

        DemandeComplementsDTO demandeComplementsDTO = super.repondreDemandeComplements(demarcheId, pkDemande,
                pkDemandeComplements, demandeComplementsReponse);

        // Récupération de la demande en BDD
        DemandeDTO demandeDTO = indexedDemandeService.getDemande(demarcheId, pkDemande);

        // Indexation
        try {
            List<DemandeFileEsDTO> files = new ArrayList<>();
            DemandeComplementsFileDTO[] fichiers = demandeComplementsDTO.getReponse().getFichiers();
            if (fichiers != null) {
                List<DemandeFileDTO> cfiles = DemandesComplementsFilesTransformer.toDemandeFileDTO(Arrays.asList(fichiers));
                if (!cfiles.isEmpty()) {
                    files.addAll(demandeFileEsTransformer.getListFileEsContent(demandeDTO, DemandeFileEsDTO.TYPE.COMPLEMENT, cfiles));
                    indexedDemandeService.indexFiles(files);
                }
            }
            indexedDemandeService.indexElement(demandeDTO, false);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'indexation du complément de la demande.");
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedDemandesComplementsServiceImpl - méthode repondreDemandeComplements()", demandeDTO, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }

        return demandeComplementsDTO;
    }

}

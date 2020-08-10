package mc.gouv.xaf.back.service.es.impl;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.es.model.EsErrorEventDTO;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.exception.AfIndexingException;
import mc.gouv.xaf.back.service.data.impl.DemandeFilesServiceImpl;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.es.handlers.EsTransactionErrorsHandler;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackFor = Exception.class)
public class IndexedEsDemandeFilesServiceImpl extends DemandeFilesServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexedEsDemandeFilesServiceImpl.class);

    @Autowired
    private IndexedDemandeService indexedDemandeService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void saveFile(DemandeFileDTO demandeFile, String demarcheId, Integer pkDemande) throws Exception {
        super.saveFile(demandeFile, demarcheId, pkDemande);
        DemandeBO demandeBo = indexedDemandeService.getDemandeBo(demarcheId, pkDemande);
        DemandeDTO demandeDto = DemandesTransformer.bo2Dto(demandeBo);
        // Indexation
        try {
            indexedDemandeService.sendToTopic(demandeFile, demandeDto);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'indexation du fichier.");
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedEsDemandeFilesServiceImpl - méthode saveFile()", demandeDto, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
    }

}

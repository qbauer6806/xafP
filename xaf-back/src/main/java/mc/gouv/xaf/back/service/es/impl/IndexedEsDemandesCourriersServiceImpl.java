package mc.gouv.xaf.back.service.es.impl;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.es.model.EsErrorEventDTO;
import mc.gouv.xaf.back.exception.AfIndexingException;
import mc.gouv.xaf.back.service.data.impl.DemandesCourriersServiceImpl;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.es.handlers.EsTransactionErrorsHandler;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

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
            indexedDemandeService.indexElement(demandeDTO, true);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'indexation du courrier.");
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedEsDemandesCourriersServiceImpl - méthode indexCourrier()", demandeDTO, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
    }
}

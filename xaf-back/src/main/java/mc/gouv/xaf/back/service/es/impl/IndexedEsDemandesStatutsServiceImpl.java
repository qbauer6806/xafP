package mc.gouv.xaf.back.service.es.impl;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.es.model.EsErrorEventDTO;
import mc.gouv.xaf.back.exception.AfIndexingException;
import mc.gouv.xaf.back.service.data.impl.DemandesStatutsServiceImpl;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.es.handlers.EsTransactionErrorsHandler;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author dsaidiparto.ext
 * <p>
 * Surcharge de DemandesStatutsServiceImpl pour indexer le statut de la demande sur Elastic Search
 */
@Service
@Primary
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackFor = Exception.class)
public class IndexedEsDemandesStatutsServiceImpl extends DemandesStatutsServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexedEsDemandesStatutsServiceImpl.class);

    @Autowired
    private IndexedDemandeService indexedDemandeService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDTO updateStatut(String demarcheId, Integer demandeId, String statut, String agentId,
                                   Integer usagerId, String codeMotif, String commentaire, String texteAEnvoyer) {

        DemandeDTO demandeDTO = null;
        try {
            demandeDTO = super.updateStatut(demarcheId, demandeId, statut, agentId, usagerId, codeMotif, commentaire, texteAEnvoyer);
        } catch (Exception ex) {
            LOGGER.error("Erreur lors de la mise à jour du statut {}", ex.getMessage());
        }
        if (null != demandeDTO) {
            indexDemandeStatus(demandeDTO);
        }
        return demandeDTO;
    }

    private void indexDemandeStatus(DemandeDTO demandeDTO) {
        LOGGER.info("Indexation de Statuts de la demande {}", demandeDTO.getPkDemandes());
        try {
            indexedDemandeService.indexElement(demandeDTO, false);
        } catch (Exception e) {
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedEsDemandesStatutsServiceImpl - méthode indexDemandeStatus()", demandeDTO, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
    }
}

package mc.gouv.xaf.back.service.es.impl;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.es.model.EsErrorEventDTO;
import mc.gouv.xaf.back.exception.AfIndexingException;
import mc.gouv.xaf.back.service.data.impl.DemandesDataServiceImpl;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.es.handlers.EsTransactionErrorsHandler;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Map;

@Primary
@Service
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackOn = Exception.class)
public class IndexedEsDemandesDataServiceImpl extends DemandesDataServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexedEsDemandesDataServiceImpl.class);

    @Autowired
    private IndexedDemandeService indexedDemandeService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public DemandeDataDTO saveOrUpdateDemandeData(String demarcheId, Integer demandeId, String key, String value)
            throws Exception {
        DemandeDataDTO demandeDataDto = super.saveOrUpdateDemandeData(demarcheId, demandeId, key, value);
        indexDemandeData(demarcheId, demandeId);
        return demandeDataDto;
    }

    @Override
    public void saveOrUpdateDemandeDatas(String demarcheId, Integer demandeId, Map<String, String> datas)
            throws Exception {
        super.saveOrUpdateDemandeDatas(demarcheId, demandeId, datas);
        indexDemandeData(demarcheId, demandeId);
    }

    @Override
    public void deleteDemandeData(String demarcheId, Integer demandeId, String key) throws Exception {
        super.deleteDemandeData(demarcheId, demandeId, key);
        indexDemandeData(demarcheId, demandeId);
    }

    private void indexDemandeData(String demarcheId, Integer demandeId) {
        DemandeDTO demandeDTO = indexedDemandeService.getDemande(demarcheId, demandeId);
        // Indexation
        try {
            indexedDemandeService.sendToTopic(demandeDTO, false);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'indexation des données de la demande.");
            EsErrorEventDTO esErrorEventDTO = EsTransactionErrorsHandler.createErrorEvent("IndexedEsDemandesDataServiceImpl - méthode indexDemandeData()", demandeDTO, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new AfIndexingException(e.getMessage(), e);
        }
    }
}

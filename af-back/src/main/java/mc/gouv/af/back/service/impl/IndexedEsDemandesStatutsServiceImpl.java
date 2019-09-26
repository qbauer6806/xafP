package mc.gouv.af.back.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.af.back.config.es.IndexationEnabledCondition;
import mc.gouv.af.back.service.IndexedDemandeService;
import mc.gouv.dem.service.impl.DemandesStatutsServiceImpl;
import mc.gouv.dem.shared.model.DemandeDTO;

/**
 * @author dsaidiparto.ext
 * 
 *         Surcharge de DemandesStatutsServiceImpl pour indexer le statut de la demande sur Elastic Search
 */
@Service
@Primary
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackFor = Exception.class)
public class IndexedEsDemandesStatutsServiceImpl extends DemandesStatutsServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexedEsDemandesStatutsServiceImpl.class);

    @Autowired
    private IndexedDemandeService indexedDemandeService;

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public DemandeDTO updateStatut(String demarcheId, Integer demandeId, String statut, String agentId,
            Integer usagerId, String codeMotif, String commentaire, String texteAEnvoyer) {

        DemandeDTO demandeDTO = null;
        try {
            demandeDTO = super.updateStatut(demarcheId, demandeId, statut, agentId, usagerId, codeMotif, commentaire, texteAEnvoyer);
            indexDemandeStatus(demandeDTO);
        } catch (Exception ex) {
            LOGGER.error("Erreur lors de la mise à jour du statut" + ex.getMessage());
        }

        return demandeDTO;
    }

    private void indexDemandeStatus(DemandeDTO demandeDTO) throws Exception {
        LOGGER.info("Indexation de Statuts de la demande " + demandeDTO.getPkDemandes());
        indexedDemandeService.sendToTopic(demandeDTO, false);
    }
}

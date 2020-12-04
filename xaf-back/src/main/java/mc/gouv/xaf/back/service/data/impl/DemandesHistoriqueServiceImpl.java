package mc.gouv.xaf.back.service.data.impl;

import java.util.Date;
import java.util.List;

import mc.gouv.xaf.back.service.es.impl.IndexedEsDemandeServiceImpl;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.data.dao.DemandesHistoriqueRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesHistoriqueBO;
import mc.gouv.xaf.back.data.transformer.DemandesHistoriqueTransformer;
import mc.gouv.xaf.back.service.data.DemandesHistoriqueService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;

/**
 * Service permettant la manipulation de l'historique des demandes.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesHistoriqueServiceImpl implements DemandesHistoriqueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesHistoriqueServiceImpl.class);

    @Autowired
    private DemandesHistoriqueRepository demandesHistoriqueRepository;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private IndexedEsDemandeServiceImpl indexedEsDemandeService;

    @Override
    public List<DemandeHistoriqueDTO> getHistorique(String demarcheId, Integer demandeId) {

        // Jette une exception si la demande n'existe pas
        demandesService.getCheckDemarcheDemandeDTO(demarcheId, demandeId, false);

        List<DemandesHistoriqueBO> demandeHistorique = demandesHistoriqueRepository.findByFkDemandesPkDemandes(demandeId);

        LOGGER.info("Transformation bo -> dto ...");
        return DemandesHistoriqueTransformer.bo2Dto(demandeHistorique);
    }

    @Override
    public DemandeHistoriqueDTO saveHistorique(String demarcheId, Integer demandeId, DemandeHistoriqueDTO demandeHistoriqueDto) {
        return saveHistoriqueActionAuto(demarcheId, demandeId, demandeHistoriqueDto);
    }

    @Override
    public DemandeHistoriqueDTO saveHistoriqueActionAuto(String demarcheId, Integer demandeId, DemandeHistoriqueDTO demandeHistoriqueDto) {
        DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, demandeId, false);

        LOGGER.info("Transformation dto -> bo");
        DemandesHistoriqueBO demandeHistoriqueBo = DemandesHistoriqueTransformer.dto2Bo(demandeHistoriqueDto);

        demandeHistoriqueBo.setFkDemandes(demandeBo);
        demandeHistoriqueBo.setFkStatut(demandeBo.getDernierStatut());
        demandeHistoriqueBo.setDate(new Date());

        LOGGER.info("Sauvegarde en base...");
        demandeHistoriqueBo = demandesHistoriqueRepository.save(demandeHistoriqueBo);

        LOGGER.info("Transformation bo -> dto ...");
        return DemandesHistoriqueTransformer.bo2Dto(demandeHistoriqueBo);
    }

    @Override
    public DemandeHistoriqueDTO saveAndIndexHistorique(DemandeHistoriqueDTO histo, String demarcheId, Integer demandeId) {
        DemandeHistoriqueDTO saved = null;
        if (histo != null) {
            LOGGER.info(SharedMessages.APPEL_SAVE_HISTORIQUE);
            try {
                saved = saveHistorique(demarcheId, demandeId, histo);
            } catch (Exception e) {
                LOGGER.error(SharedMessages.ERREUR_HISTORIQUE, histo, e);
            }
            if (StringUtils.isNotBlank(histo.getJustificatifTraitement())) {
                DemandeDTO demande = demandesService.getDemande(demarcheId, demandeId);
                indexedEsDemandeService.indexDemande(demande);
            }
        }
        return saved;
    }

}

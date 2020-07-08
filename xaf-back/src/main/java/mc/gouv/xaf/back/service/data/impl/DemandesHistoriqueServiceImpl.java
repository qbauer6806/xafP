package mc.gouv.xaf.back.service.data.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.data.dao.DemandesHistoriqueRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesHistoriqueBO;
import mc.gouv.xaf.back.data.transformer.DemandesHistoriqueTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.DemandesHistoriqueService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;

/**
 * 
 * Service permettant la manipulation de l'historique des demandes.
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesHistoriqueServiceImpl implements DemandesHistoriqueService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesHistoriqueServiceImpl.class);

    @Autowired
    private DemandesHistoriqueRepository demandesHistoriqueRepository;
    
    @Autowired
    private DemandesService demandesService;
    
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
        
        if ((StringUtils.isBlank(demandeHistoriqueDto.getAgentId()) && demandeHistoriqueDto.getUsagerId() == null) ||
                (!StringUtils.isBlank(demandeHistoriqueDto.getAgentId()) && demandeHistoriqueDto.getUsagerId() != null)) {
            throw new DemarchesServiceException("Soit agentId ou usagerId doit être renseigné", HttpStatus.BAD_REQUEST);
        }

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

}

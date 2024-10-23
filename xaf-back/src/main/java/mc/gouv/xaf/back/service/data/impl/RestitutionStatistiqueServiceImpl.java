package mc.gouv.xaf.back.service.data.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.data.dao.RestitutionStatistiquesRepository;
import mc.gouv.xaf.back.data.entity.RestitutionStatistiquesBO;
import mc.gouv.xaf.back.data.transformer.RestitutionStatistiquesTransformer;
import mc.gouv.xaf.back.service.data.RestitutionStatistiquesService;
import mc.gouv.xaf.shared.dto.RestitutionStatistiquesDTO;

/**
 * Service permettant la manipulation des statistiques liés à la restitution des données.
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class RestitutionStatistiqueServiceImpl implements RestitutionStatistiquesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestitutionStatistiqueServiceImpl.class);

    @Autowired
    private RestitutionStatistiquesRepository restitutionStatRepository;

    public void saveRestitutionStatistique(RestitutionStatistiquesDTO restitutionStat) {
        LOGGER.info("Création d'une ligne de statistique pour l'usager {}", restitutionStat.getUsagerId());
        RestitutionStatistiquesBO bo = RestitutionStatistiquesTransformer.dto2Bo(restitutionStat);
        restitutionStatRepository.save(bo);
    }
}

package mc.gouv.xaf.back.service.data.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.StatistiquesRepository;
import mc.gouv.xaf.back.data.entity.StatistiqueBO;
import mc.gouv.xaf.back.data.transformer.StatistiqueTransformer;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.StatistiquesService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.StatistiqueDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des statistiques.
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class StatistiquesServiceImpl implements StatistiquesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatistiquesServiceImpl.class);

    private final StatistiquesRepository statRepository;
    private final GouvPropertiesResolver gouvPropertiesResolver;

    /**
     * {@inheritDoc}
     */
    @Override
    public StatistiqueDTO saveStatistique(StatistiqueDTO stat) {

        LOGGER.info("Création d'une statistique pour la demande {}", stat.getDemandeId());
        List<StatistiqueBO> statistiquesBO = statRepository.findByDemandeId(stat.getDemandeId());

        if (!statistiquesBO.isEmpty()) {
            StatistiqueBO derniereStat = statistiquesBO.getLast();

            // On ne crée pas de nouvelle information si le dernier statut est le même l'actuel
            if (derniereStat.getStatutPublic().equals(stat.getStatutPublic())) {
                LOGGER.info("Une stat a déjà été trouvée avec le même statut - On ne crée pas de nouvelle statistique");
                return StatistiqueTransformer.bo2Dto(derniereStat);
            }
        }

        StatistiqueBO bo = StatistiqueTransformer.dto2Bo(stat);
        bo = statRepository.save(bo);

        return StatistiqueTransformer.bo2Dto(bo);
    }

    @Override
    public StatistiqueDTO saveStatistique(DemandeDTO demandeDTO) {
        StatistiqueDTO statistiqueDTO = new StatistiqueDTO();
        statistiqueDTO.setDemandeId(demandeDTO.getPkDemandes());
        statistiqueDTO.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
        statistiqueDTO.setStatutPublic(demandeDTO.getDernierStatut().getName());
        statistiqueDTO.setCanal(demandeDTO.getCanal().name());
        statistiqueDTO.setDate(demandeDTO.getDernierStatut().getDate());
        statistiqueDTO.setIdentifiantDemande(demandeDTO.getIdentifiant());
        statistiqueDTO.setTypeConnexionUsager(demandeDTO.getTypeConnexionUsager());
        return saveStatistique(statistiqueDTO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteStatistique(Integer statId) {
        statRepository.deleteById(statId);
    }

    @Override
    public void deleteStatistiques(Integer pkDemande) {
        LOGGER.info("Suppression des statistiques de la demande {}", pkDemande);
        List<StatistiqueBO> statistiquesBO = statRepository.findByDemandeId(pkDemande);
        statRepository.deleteAll(statistiquesBO);
    }

}

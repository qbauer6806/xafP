//package mc.gouv.xaf.back.service.data.impl;
//
//import mc.gouv.xaf.back.data.dao.RestitutionStatistiquesRepository;
//import mc.gouv.xaf.back.data.dao.StatistiquesRepository;
//import mc.gouv.xaf.back.data.entity.StatistiqueBO;
//import mc.gouv.xaf.back.data.transformer.StatistiqueTransformer;
//import mc.gouv.xaf.back.service.data.StatistiquesService;
//import mc.gouv.xaf.shared.dto.DemandeDTO;
//import mc.gouv.xaf.shared.dto.StatistiqueDTO;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
///**
// * Service permettant la manipulation des statistiques liés à la restitution des données.
// */
//@Component
//@Transactional(rollbackFor = Exception.class)
//public class RestitutionStatistiqueServiceImpl implements StatistiquesService {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(RestitutionStatistiqueServiceImpl.class);
//
//    @Autowired
//    private RestitutionStatistiquesRepository restitutionStatRepository;
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public StatistiqueDTO saveStatistique(StatistiqueDTO stat) {
//
//        LOGGER.info("Création d'une statistique pour la demande {}", stat.getDemandeId());
//        List<StatistiqueBO> statistiquesBO = statRepository.findByDemandeIdAndDemarcheId(stat.getDemandeId(), stat.getDemarcheId());
//
//        if (!statistiquesBO.isEmpty()) {
//            StatistiqueBO derniereStat = statistiquesBO.get(statistiquesBO.size()-1);
//
//            // On ne crée pas de nouvelle information si le dernier statut est le même l'actuel
//            if (derniereStat.getStatutPublic().equals(stat.getStatutPublic())) {
//                LOGGER.info("Une stat a déjà été trouvée avec le même statut - On ne crée pas de nouvelle statistique");
//                return StatistiqueTransformer.bo2Dto(derniereStat);
//            }
//        }
//
//        StatistiqueBO bo = StatistiqueTransformer.dto2Bo(stat);
//        bo = statRepository.save(bo);
//
//        return StatistiqueTransformer.bo2Dto(bo);
//    }
//
//    @Override
//    public StatistiqueDTO saveStatistique(DemandeDTO demandeDTO) {
//        StatistiqueDTO statistiqueDTO = new StatistiqueDTO();
//        statistiqueDTO.setDemandeId(demandeDTO.getPkDemandes());
//        statistiqueDTO.setDemarcheId(demandeDTO.getDemarcheId());
//        statistiqueDTO.setStatutPublic(demandeDTO.getDernierStatut().getLibelle());
//        statistiqueDTO.setCanal(demandeDTO.getCanal().name());
//        statistiqueDTO.setDate(demandeDTO.getDernierStatut().getDate());
//        statistiqueDTO.setIdentifiantDemande(demandeDTO.getIdentifiant());
//        return saveStatistique(statistiqueDTO);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void deleteStatistique(Integer statId) {
//        statRepository.deleteById(statId);
//    }
//    
//    @Override
//    public void deleteStatistiques(String demarcheId, Integer pkDemande) {
//        LOGGER.info("Suppression des statistiques de la demande {}", pkDemande);
//        List<StatistiqueBO> statistiquesBO = statRepository.findByDemandeIdAndDemarcheId(pkDemande, demarcheId);
//        statRepository.deleteAll(statistiquesBO);
//    }
//
//
//}

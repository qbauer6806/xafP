package mc.gouv.xaf.back.service.data.impl;

import mc.gouv.xaf.back.data.dao.StatistiquesRepository;
import mc.gouv.xaf.back.data.entity.StatistiqueBO;
import mc.gouv.xaf.back.data.transformer.StatistiqueTransformer;
import mc.gouv.xaf.back.service.data.PurgeService;
import mc.gouv.xaf.back.service.data.StatistiquesService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PurgeDemandeDTO;
import mc.gouv.xaf.shared.dto.StatistiqueDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service permettant la manipulation des statistiques.
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class PurgeServiceImpl implements PurgeService {

    private final Integer OFFSET_MOIS_DATE_PURGE = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(PurgeServiceImpl.class);

    @Autowired
    private StatistiquesRepository statRepository;

    public List<PurgeDemandeDTO> getDemandesPurgees() {
        LOGGER.info("Récupération des demandes purgées à moins {} mois", OFFSET_MOIS_DATE_PURGE);
        Date dateDebutOffset = Date.from(LocalDateTime.now().minusMonths(OFFSET_MOIS_DATE_PURGE).atZone(ZoneId.systemDefault()).toInstant());
        List<StatistiqueBO> statsDemandesPurgees = statRepository.findByStatutPublicAndDateBetween(AfBackUtils.STATUT_PUBLIC_SUPPRIMEE,
                dateDebutOffset , new Date());
        statsDemandesPurgees.sort(Comparator.comparing(StatistiqueBO::getDate));

        List<PurgeDemandeDTO> demandesPurgees = new ArrayList<>();
        for(StatistiqueBO stat : statsDemandesPurgees) {
            PurgeDemandeDTO purgeDemandeDTO = new PurgeDemandeDTO();
            purgeDemandeDTO.setIdentifiantDemande(stat.getIdentifiantDemande());
            purgeDemandeDTO.setDateSuppression(stat.getDate());

            // Recherche du dernier statut non supprimé pour la stat en question
            StatistiqueBO statDernierStatut = statRepository.findFirstByDemandeIdAndStatutPublicNotOrderByDateDesc(stat.getDemandeId(),
                    AfBackUtils.STATUT_PUBLIC_SUPPRIMEE);
            purgeDemandeDTO.setDateStatutFinal(statDernierStatut.getDate());
            demandesPurgees.add(purgeDemandeDTO);
        }

        return demandesPurgees;
    }
}

package mc.gouv.xaf.back.service.impl;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import mc.gouv.xaf.back.data.dao.DemandesStatistiquesInternesRepository;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatistiquesServiceImplTest {

    private final static Long COUNT_ATTENTE_TRAITEMENT = 1L;

    private final static Long COUNT_TRAITEMENT = 2L;

    private final static Long COUNT_ATTENTE_INFOS = 3L;

    private final static String EN_ATTENTE_TRAITENUM = "EN_ATTENTE_TRAIT_ENUM";

    private final static String EN_COURS_TRAIT_ENUM = "EN_COURS_TRAIT_ENUM";

    private final static String EN_ATTENTE_COMPL_ENUM = "EN_ATTENTE_COMPL_ENUM";

    @InjectMocks
    private StatistiquesInternesServiceImpl statistiquesService;

    @Mock
    private DemandesStatistiquesInternesRepository demandesStatistiquesInternesRepository;

    @Mock
    private DemarchesDataProvider demarchesDataProvider;

    @BeforeEach
    void setUp() {

        Mockito.lenient().when(demandesStatistiquesInternesRepository.countByCanalAndDernierStatutName(
                DemandeCanalEnum.GUICHET_VIRTUEL.name(), EN_ATTENTE_TRAITENUM)).thenReturn(COUNT_ATTENTE_TRAITEMENT);
        Mockito.lenient().when(demandesStatistiquesInternesRepository.countByCanalAndDernierStatutName(
                DemandeCanalEnum.GUICHET_VIRTUEL.name(), EN_COURS_TRAIT_ENUM)).thenReturn(COUNT_TRAITEMENT);
        Mockito.lenient().when(demandesStatistiquesInternesRepository.countByCanalAndDernierStatutName(
                DemandeCanalEnum.GUICHET_PHYSIQUE.name(), EN_COURS_TRAIT_ENUM)).thenReturn(COUNT_TRAITEMENT);
        Mockito.lenient().when(demandesStatistiquesInternesRepository.countByCanalAndDernierStatutName(
                DemandeCanalEnum.COURRIER.name(), EN_ATTENTE_COMPL_ENUM)).thenReturn(COUNT_ATTENTE_INFOS);

        Map<String, String> statusMap = new HashMap<>();
        statusMap.put(EN_ATTENTE_TRAITENUM, EN_ATTENTE_TRAITENUM);
        statusMap.put(EN_COURS_TRAIT_ENUM, EN_COURS_TRAIT_ENUM);
        statusMap.put(EN_ATTENTE_COMPL_ENUM, EN_ATTENTE_COMPL_ENUM);
        Mockito.when(demarchesDataProvider.getStatusMap()).thenReturn(statusMap);
    }

    @Test
    void getStatsModel_check_by_status() {
        Map<String, Map<String, Long>> map = statistiquesService.getNumberOfEachDemandes();
        Map<String, Long> mapGuichetVirtuel = map.get(DemandeCanalEnum.GUICHET_VIRTUEL.name());
        Map<String, Long> mapGuichetPhysique = map.get(DemandeCanalEnum.GUICHET_PHYSIQUE.name());
        Map<String, Long> mapGuichetCourrier = map.get(DemandeCanalEnum.COURRIER.name());

        assertEquals(1L, mapGuichetVirtuel.get(EN_ATTENTE_TRAITENUM).longValue());
        assertEquals(2L, mapGuichetVirtuel.get(EN_COURS_TRAIT_ENUM).longValue());
        assertEquals(0L, mapGuichetVirtuel.get(EN_ATTENTE_COMPL_ENUM).longValue());

        assertEquals(0L, mapGuichetPhysique.get(EN_ATTENTE_TRAITENUM).longValue());
        assertEquals(2L, mapGuichetPhysique.get(EN_COURS_TRAIT_ENUM).longValue());
        assertEquals(0L, mapGuichetPhysique.get(EN_ATTENTE_COMPL_ENUM).longValue());

        assertEquals(0L, mapGuichetCourrier.get(EN_ATTENTE_TRAITENUM).longValue());
        assertEquals(0L, mapGuichetCourrier.get(EN_COURS_TRAIT_ENUM).longValue());
        assertEquals(3L, mapGuichetCourrier.get(EN_ATTENTE_COMPL_ENUM).longValue());

    }

    @Test
    void getStatsModel_check_totals() {
        Map<String, Map<String, Long>> map = statistiquesService.getNumberOfEachDemandes();
        Map<String, Long> mapGuichetVirtuel = map.get(DemandeCanalEnum.GUICHET_VIRTUEL.name());
        Map<String, Long> mapGuichetPhysique = map.get(DemandeCanalEnum.GUICHET_PHYSIQUE.name());
        Map<String, Long> mapGuichetCourrier = map.get(DemandeCanalEnum.COURRIER.name());
        Map<String, Long> mapTotal = map.get("TOTAL");

        assertEquals(3L, mapGuichetVirtuel.get("TOTAL").longValue());
        assertEquals(2L, mapGuichetPhysique.get("TOTAL").longValue());
        assertEquals(3L, mapGuichetCourrier.get("TOTAL").longValue());

        assertEquals(1L, mapTotal.get(EN_ATTENTE_TRAITENUM).longValue());
        assertEquals(4L, mapTotal.get(EN_COURS_TRAIT_ENUM).longValue());
        assertEquals(3L, mapTotal.get(EN_ATTENTE_COMPL_ENUM).longValue());
    }
}

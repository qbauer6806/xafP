package mc.gouv.xaf.back.data.transformer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import mc.gouv.xaf.back.data.entity.StatistiqueBO;
import mc.gouv.xaf.shared.dto.StatistiqueDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatistiqueTransformerTest {

    @Test
    void testBo2Dto() {
        StatistiqueBO statistiqueBO = new StatistiqueBO();
        assertNull(StatistiqueTransformer.bo2Dto(null));
        assertNotNull(StatistiqueTransformer.bo2Dto(statistiqueBO));
    }

    @Test
    void testDto2Bo() {
        StatistiqueDTO statistiqueDTO = new StatistiqueDTO();

        assertNull(StatistiqueTransformer.dto2Bo(null));
        assertNotNull(StatistiqueTransformer.dto2Bo(statistiqueDTO));
    }

    @Test
    void testTypeConnexionUsagerBo() {
        StatistiqueBO statistiqueBO = new StatistiqueBO();
        // Dans le cas où il y aurait par exemple, une mauvaise valeur en BDD
        statistiqueBO.setTypeConnexionUsager("ERREUR ENUM");

        assertThrows(IllegalArgumentException.class, () -> StatistiqueTransformer.bo2Dto(statistiqueBO));
    }
}

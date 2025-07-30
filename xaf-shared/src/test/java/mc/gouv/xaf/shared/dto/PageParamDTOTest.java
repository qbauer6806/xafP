package mc.gouv.xaf.shared.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class PageParamDTOTest {

    @Test
    void newPageParamDTOTest() {
        PageParamDTO paramDTO = new PageParamDTO();
        assertEquals(0, paramDTO.getPage());
        assertEquals(10, paramDTO.getSize());
        assertEquals("identifiant", paramDTO.getSort());
        assertEquals("ASC", paramDTO.getDirection());
        assertEquals("fr", paramDTO.getLang());
        assertNotNull(paramDTO.getStatus());
        assertEquals("[]", paramDTO.getStatus());
    }

    @Test
    void newPageParamDTOWithParamTest() {
        List<String> statuts = List.of("REFUSEE", "VALIDEE");
        PageParamDTO paramDTO = new PageParamDTO(2, 2, "date", "DESC", statuts, "en", null);
        assertEquals(2, paramDTO.getPage());
        assertEquals(2, paramDTO.getSize());
        assertEquals("date", paramDTO.getSort());
        assertEquals("DESC", paramDTO.getDirection());
        assertEquals("en", paramDTO.getLang());
    }

    @Test
    void setDirectionTestNull() {
        PageParamDTO paramDTO = new PageParamDTO();
        paramDTO.setDirection(null);
        assertEquals("ASC", paramDTO.getDirection());
    }

    @Test
    void setDirectionTestUnknown() {
        PageParamDTO paramDTO = new PageParamDTO();
        paramDTO.setDirection("blablabla");
        assertEquals("ASC", paramDTO.getDirection());
    }

    @Test
    void setDirectionTestASC() {
        PageParamDTO paramDTO = new PageParamDTO();
        paramDTO.setDirection("ASC");
        assertEquals("ASC", paramDTO.getDirection());
    }

    @Test
    void setDirectionTestLowerCase() {
        PageParamDTO paramDTO = new PageParamDTO();
        paramDTO.setDirection("desc");
        assertEquals("DESC", paramDTO.getDirection());
    }

}

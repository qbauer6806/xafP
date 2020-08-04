package mc.gouv.xaf.shared.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PageParamDTOTest {

    @Test
    public void newPageParamDTOTest() {
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
    public void newPageParamDTOWithParamTest() {
        PageParamDTO paramDTO = new PageParamDTO(2, 2, "date", "DESC", "[\"REFUSEE\",\"VALIDEE\"]", "en");
        assertEquals(2, paramDTO.getPage());
        assertEquals(2, paramDTO.getSize());
        assertEquals("date", paramDTO.getSort());
        assertEquals("DESC", paramDTO.getDirection());
        String status = paramDTO.getStatus();
        assertEquals("[\"REFUSEE\",\"VALIDEE\"]", status);
        String[] statusArray = paramDTO.getStatusArray();
        assertEquals(2, statusArray.length);
        assertEquals("REFUSEE", statusArray[0]);
        assertEquals("VALIDEE", statusArray[1]);
        assertEquals("en", paramDTO.getLang());
    }

    @Test
    public void setDirectionTestNull() {
        PageParamDTO paramDTO = new PageParamDTO();
        paramDTO.setDirection(null);
        assertEquals("ASC", paramDTO.getDirection());
    }

    @Test
    public void setDirectionTestUnknown() {
        PageParamDTO paramDTO = new PageParamDTO();
        paramDTO.setDirection("blablabla");
        assertEquals("ASC", paramDTO.getDirection());
    }

    @Test
    public void setDirectionTestASC() {
        PageParamDTO paramDTO = new PageParamDTO();
        paramDTO.setDirection("ASC");
        assertEquals("ASC", paramDTO.getDirection());
    }

    @Test
    public void setDirectionTestLowerCase() {
        PageParamDTO paramDTO = new PageParamDTO();
        paramDTO.setDirection("desc");
        assertEquals("DESC", paramDTO.getDirection());
    }

}
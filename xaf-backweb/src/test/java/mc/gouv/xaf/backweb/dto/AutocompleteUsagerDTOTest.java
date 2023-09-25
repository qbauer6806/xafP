package mc.gouv.xaf.backweb.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AutocompleteUsagerDTOTest {

    @Test
    public void testEquals() {
        AutocompleteUsagerDTO dto = new AutocompleteUsagerDTO();
        assertEquals(dto, dto);
    }

    @Test
    public void testEqualsDeuxObjets() {
        AutocompleteUsagerDTO dto1 = new AutocompleteUsagerDTO();
        AutocompleteUsagerDTO dto2 = new AutocompleteUsagerDTO();
        assertNotEquals(dto1, dto2);
        assertNotEquals(dto2, dto1);
    }

}

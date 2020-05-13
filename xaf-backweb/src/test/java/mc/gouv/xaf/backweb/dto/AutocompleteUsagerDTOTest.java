package mc.gouv.xaf.backweb.dto;

import org.junit.Assert;
import org.junit.Test;

public class AutocompleteUsagerDTOTest {

    @Test
    public void testEquals() {
        AutocompleteUsagerDTO dto = new AutocompleteUsagerDTO();
        Assert.assertTrue(dto.equals(dto));
    }

    @Test
    public void testEqualsDeuxObjets() {
        AutocompleteUsagerDTO dto1 = new AutocompleteUsagerDTO();
        AutocompleteUsagerDTO dto2 = new AutocompleteUsagerDTO();
        Assert.assertFalse(dto1.equals(dto2));
        Assert.assertFalse(dto2.equals(dto1));
    }

}

package mc.gouv.xaf.back.data.es.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import mc.gouv.xaf.back.data.model.RechercheCategoryDTO;
import org.junit.jupiter.api.Test;

class RechercheCategoryDTOTest {

    @Test
    void equalsTest() {
        RechercheCategoryDTO cat = new RechercheCategoryDTO(1, "Cat1", true);
        assertEquals(cat, cat);
    }

    @Test
    void equalsDeuxCatergoriesTest() {
        RechercheCategoryDTO cat1 = new RechercheCategoryDTO(1, "Cat1", true);
        RechercheCategoryDTO cat2 = new RechercheCategoryDTO(1, "Cat1", true);
        assertNotEquals(cat1, cat2);
        assertNotEquals(cat2, cat1);
    }

}

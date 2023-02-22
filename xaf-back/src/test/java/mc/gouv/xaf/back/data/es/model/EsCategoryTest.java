package mc.gouv.xaf.back.data.es.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class EsCategoryTest {

    @Test
    public void equalsTest() {
        EsCategory cat = new EsCategory(1, "Cat1", true);
        assertEquals(cat, cat);
    }

    @Test
    public void equalsDeuxCatergoriesTest() {
        EsCategory cat1 = new EsCategory(1, "Cat1", true);
        EsCategory cat2 = new EsCategory(1, "Cat1", true);
        assertNotEquals(cat1, cat2);
        assertNotEquals(cat2, cat1);
    }

    @Test
    public void equalsConstructeurVideTest() {
        EsCategory cat = new EsCategory();
        assertEquals(cat, cat);
    }

}

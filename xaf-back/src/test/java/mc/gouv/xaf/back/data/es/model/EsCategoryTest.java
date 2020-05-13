package mc.gouv.xaf.back.data.es.model;

import org.junit.Assert;
import org.junit.Test;

public class EsCategoryTest {

    @Test
    public void equalsTest() {
        EsCategory cat = new EsCategory(1, "Cat1", true);
        Assert.assertTrue(cat.equals(cat));
    }

    @Test
    public void equalsDeuxCatergoriesTest() {
        EsCategory cat1 = new EsCategory(1, "Cat1", true);
        EsCategory cat2 = new EsCategory(1, "Cat1", true);
        Assert.assertFalse(cat1.equals(cat2));
        Assert.assertFalse(cat2.equals(cat1));
    }

    @Test
    public void equalsConstructeurVideTest() {
        EsCategory cat = new EsCategory();
        Assert.assertTrue(cat.equals(cat));
    }

}

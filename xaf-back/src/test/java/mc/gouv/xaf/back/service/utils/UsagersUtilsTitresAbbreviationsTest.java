package mc.gouv.xaf.back.service.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(Parameterized.class)
public class UsagersUtilsTitresAbbreviationsTest {

    @Parameterized.Parameter(0)
    public Integer titre;
    @Parameterized.Parameter(1)
    public String abbreviation;

    @Parameterized.Parameters
    public static Object[][] data() {
        return new Object[][]{
                {0, "Mr"},
                {1, "Mme"},
                {2, "Mlle"}
        };
    }

    @Test
    public void titreToAbbreviationTest() {
        String resultat = UsagersUtils.titreToAbbreviation(titre);
        assertEquals(abbreviation, resultat);
    }

    @Test
    public void abbreviationToTitreTest() {
        Integer resultat = UsagersUtils.abbreviationToTitre(abbreviation);
        assertEquals(titre, resultat);
    }

}

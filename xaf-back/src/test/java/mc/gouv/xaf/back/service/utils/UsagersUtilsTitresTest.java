package mc.gouv.xaf.back.service.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(Parameterized.class)
public class UsagersUtilsTitresTest {

    @Parameterized.Parameter(0)
    public short titre;
    @Parameterized.Parameter(1)
    public String expected;

    @Parameterized.Parameters
    public static Object[][] data() {
        return new Object[][]{
                {(short) 0, "Monsieur"},
                {(short) 1, "Madame"},
                {(short) 2, "Mademoiselle"},
                {(short) 3, "Madame, Monsieur"}
        };
    }

    @Test
    public void getUsagerTitreTest() {
        String resultat = UsagersUtils.titreShortToString(titre);
        assertEquals(expected, resultat);
    }

}

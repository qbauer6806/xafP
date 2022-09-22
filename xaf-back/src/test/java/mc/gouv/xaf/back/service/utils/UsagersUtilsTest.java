package mc.gouv.xaf.back.service.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UsagersUtilsTest {
    @Test
    public void titreToStringNullTest() {
        String resultat = UsagersUtils.titreShortToString(null);
        assertEquals("Madame, Monsieur", resultat);
    }

    @Test
    public void titreToAbbreviationTitreNull() {
        String resultat = UsagersUtils.titreToAbbreviation(null);
        assertNull(resultat);
    }

    @Test
    public void titreToAbbreviationFakeData() {
        String resultat = UsagersUtils.titreToAbbreviation(-1);
        assertNull(resultat);
    }

    @Test
    public void abbreviationToTitreAbbrNull() {
        Integer resultat = UsagersUtils.abbreviationToTitre(null);
        assertNull(resultat);
    }

    @Test
    public void abbreviationToTitreEmptyString() {
        Integer resultat = UsagersUtils.abbreviationToTitre("");
        assertNull(resultat);
    }

    @Test
    public void abbreviationToTitreFakeData() {
        Integer resultat = UsagersUtils.abbreviationToTitre("fake");
        assertNull(resultat);
    }

}

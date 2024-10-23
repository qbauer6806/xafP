package mc.gouv.xaf.back.service.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UsagersUtilsTest {

    @Test
    void titreToStringNullTest() {
        String resultat = UsagersUtils.titreShortToString(null);
        assertEquals("Madame, Monsieur", resultat);
    }

    @Test
    void titreToAbbreviationTitreNull() {
        String resultat = UsagersUtils.titreToAbbreviation(null);
        assertNull(resultat);
    }

    @Test
    void titreToAbbreviationFakeData() {
        String resultat = UsagersUtils.titreToAbbreviation(-1);
        assertNull(resultat);
    }

    @ParameterizedTest
    @ValueSource(strings = { "null", "", "fake" })
    void abbreviationToTitre(String str) {
        Integer resultat;
        if (StringUtils.equals("null", str)) {
            resultat = UsagersUtils.abbreviationToTitre(null);
        } else {
            resultat = UsagersUtils.abbreviationToTitre(str);
        }
        assertNull(resultat);
    }

}

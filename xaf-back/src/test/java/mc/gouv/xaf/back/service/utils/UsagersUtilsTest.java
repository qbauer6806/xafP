package mc.gouv.xaf.back.service.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UsagersUtilsTest {
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

    @ParameterizedTest
    @ValueSource(strings = {"null", "", "fake"})
    void abbreviationToTitre(String str) {
    	Integer resultat;
    	if (StringUtils.equals("null", str)) {
    		resultat = UsagersUtils.abbreviationToTitre(null);
    	}
    	else {
    		resultat = UsagersUtils.abbreviationToTitre(str);
    	}
        assertNull(resultat);
    }

}

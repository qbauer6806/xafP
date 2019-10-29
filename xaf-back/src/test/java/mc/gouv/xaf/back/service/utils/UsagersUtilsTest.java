package mc.gouv.xaf.back.service.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UsagersUtilsTest {

    @InjectMocks
    private UsagersUtils usagersUtils;

    @Test
    public void titreToAbbreviation_titreNull() {
        String resultat = usagersUtils.titreToAbbreviation(null);
        assertNull(resultat);
    }

    @Test
    public void titreToAbbreviation_fakeData() {
        String resultat = usagersUtils.titreToAbbreviation(-1);
        assertNull(resultat);
    }

    @Test
    public void titreToAbbreviation_monsieur() {
        Integer titre = new Integer(AfBackUtils.GENDER_MR_INDEX);
        String resultat = usagersUtils.titreToAbbreviation(titre);
        String expected = "Mr";
        assertEquals(expected, resultat);
    }

    @Test
    public void titreToAbbreviation_madame() {
        Integer titre = new Integer(AfBackUtils.GENDER_MME_INDEX);
        String resultat = usagersUtils.titreToAbbreviation(titre);
        String expected = "Mme";
        assertEquals(expected, resultat);
    }

    @Test
    public void titreToAbbreviation_mademoiselle() {
        Integer titre = new Integer(AfBackUtils.GENDER_MLLE_INDEX);
        String resultat = usagersUtils.titreToAbbreviation(titre);
        String expected = "Mlle";
        assertEquals(expected, resultat);
    }

    @Test
    public void abbreviationToTitre_abbrNull() {
        Integer resultat = usagersUtils.abbreviationToTitre(null);
        assertNull(resultat);
    }

    @Test
    public void abbreviationToTitre_emptyString() {
        Integer resultat = usagersUtils.abbreviationToTitre("");
        assertNull(resultat);
    }

    @Test
    public void abbreviationToTitre_fakeData() {
        Integer resultat = usagersUtils.abbreviationToTitre("fake");
        assertNull(resultat);
    }

    @Test
    public void abbreviationToTitre_monsieur() {
        String abbr = "Mr";
        Integer resultat = usagersUtils.abbreviationToTitre(abbr);
        Integer expected = new Integer(AfBackUtils.GENDER_MR_INDEX);
        assertEquals(expected, resultat);
    }

    @Test
    public void abbreviationToTitre_madame() {
        String abbr = "Mme";
        Integer resultat = usagersUtils.abbreviationToTitre(abbr);
        Integer expected = new Integer(AfBackUtils.GENDER_MME_INDEX);
        assertEquals(expected, resultat);
    }

    @Test
    public void abbreviationToTitre_mademoiselle() {
        String abbr = "Mlle";
        Integer resultat = usagersUtils.abbreviationToTitre(abbr);
        Integer expected = new Integer(AfBackUtils.GENDER_MLLE_INDEX);
        assertEquals(expected, resultat);
    }

}

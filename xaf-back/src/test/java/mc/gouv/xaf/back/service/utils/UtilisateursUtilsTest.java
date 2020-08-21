package mc.gouv.xaf.back.service.utils;

import mc.gouv.logon.shared.User;
import mc.gouv.logon.shared.User.Civilite;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class UtilisateursUtilsTest {

    private static final String MATRICULE = "1";
    private static final String PRENOM = "Toto";
    private static final String NOM = "Titi";
    private static final String MATRICULE_DAME = "2";
    private static final String PRENOM_DAME = "Ma";
    private static final String NOM_DAME = "Dame";
    private static final String NOM_NAISSANCE = "Naissance";
    private static final String MATRICULE_SC = "3";
    private static final String PRENOM_SC = "To\u001Cto";
    private static final String NOM_SC = "Ti\u001Cti";

    // Données fausses
    private static final String MAUVAIS_MATRICULE = "0";

    // Données de l'utilisateur 1
    private User user_monsieur;

    // Données de l'utilisateur 2
    private User user_dame;

    // Données de l'utilisateur 3 (avec caractère spécial '\u001C')
    private User user_sc;

    @InjectMocks
    private UtilisateursUtils utilisateursUtils;

    @Mock
    private UtilisateursCache utilisateursCache;

    private User createUser(String matricule, String prenom, String nom, String nomUsage, String nomNaissance, Civilite civ) {
        User user = new User();
        user.resetData();
        user.setMatricule(matricule);
        user.setPrenom(prenom);
        user.setNomAffichage(nom);
        user.setNomUsage(nomUsage);
        user.setNomNaissance(nomNaissance);
        user.setCivilite(civ);
        return user;
    }

    @Before
    public void setUp() {
        user_monsieur = createUser(MATRICULE, PRENOM, NOM, NOM, NOM, Civilite.MONSIEUR);
        user_dame = createUser(MATRICULE_DAME, PRENOM_DAME, NOM_DAME, null, NOM_NAISSANCE, Civilite.MADAME);
        user_sc = createUser(MATRICULE_SC, PRENOM_SC, NOM_SC, NOM_SC, NOM_SC, Civilite.MONSIEUR);
        Mockito.when(utilisateursCache.get(MATRICULE)).thenReturn(user_monsieur);
        Mockito.when(utilisateursCache.get(MATRICULE_SC)).thenReturn(user_sc);
        Mockito.when(utilisateursCache.get(MAUVAIS_MATRICULE)).thenReturn(null);
    }

    @Test
    public void getUserNameFromIDTestBonUtilisateur() {
        String expected = PRENOM + " " + NOM;
        String nomPrenom = utilisateursUtils.getUserNameFromID(MATRICULE);
        assertEquals(expected, nomPrenom);
    }

    @Test
    public void getUserNameFromIDTestBonUtilisateurSpecialChars() {
        String nomPrenom = utilisateursUtils.getUserNameFromID(MATRICULE_SC);
        assertEquals("To to Ti ti", nomPrenom);
    }

    @Test
    public void getUserNameFromIDTestMauvaisMatricule() {
        String nomPrenom = utilisateursUtils.getUserNameFromID(MAUVAIS_MATRICULE);
        assertNull(nomPrenom);
    }

    @Test
    public void getUserFullNameFromUserTestBonUtilisateur() {
        String expected = "M. " + PRENOM + " " + NOM;
        String fullname = utilisateursUtils.getUserFullNameFromUser(user_monsieur);
        assertEquals(expected, fullname);
    }

    @Test
    public void getUserFullNameFromUserTestBonUtilisateurSpecialChars() {
        String fullname = utilisateursUtils.getUserFullNameFromUser(user_sc);
        assertEquals("M. To to Ti ti", fullname);
    }

    @Test
    public void getUserFullNameFromUserTestJeuneFille() {
        String expected = "Mme. " + PRENOM_DAME + " " + NOM_NAISSANCE;
        String fullname = utilisateursUtils.getUserFullNameFromUser(user_dame);
        assertEquals(expected, fullname);
    }

    @Test
    public void getUserFullNameFromUserTestMariee() {
        String expected = "Mme. " + PRENOM_DAME + " " + NOM_DAME;
        user_dame.setNomUsage(NOM_DAME);
        String fullname = utilisateursUtils.getUserFullNameFromUser(user_dame);
        assertEquals(expected, fullname);
    }

    @Test
    public void getUserFullNameFromUserTestNull() {
        String expected = "";
        User user = createUser(MATRICULE, null, null, null, null, null);
        String fullname = utilisateursUtils.getUserFullNameFromUser(user);
        assertEquals(expected, fullname);
    }

    @Test
    public void getUserFullNameFromUserTestNomSeulement() {
        User user = createUser(MATRICULE, null, NOM, NOM, NOM, null);
        String fullname = utilisateursUtils.getUserFullNameFromUser(user);
        assertEquals(NOM, fullname);
    }

    @Test
    public void getUserFullNameFromUserTestPrenomSeulement() {
        String expected = PRENOM + " ";
        User user = createUser(MATRICULE, PRENOM, null, null, null, null);
        String fullname = utilisateursUtils.getUserFullNameFromUser(user);
        assertEquals(expected, fullname);
    }

    @Test
    public void getUserFullNameFromUserTestCivSeulement() {
        String expected = "M. ";
        User user = createUser(MATRICULE, null, null, null, null, Civilite.MONSIEUR);
        String fullname = utilisateursUtils.getUserFullNameFromUser(user);
        assertEquals(expected, fullname);
    }

    @Test
    public void getUserFullNameFromUserTestPrenomNull() {
        String expected = "M. " + NOM;
        User user = createUser(MATRICULE, null, NOM, NOM, NOM, Civilite.MONSIEUR);
        String fullname = utilisateursUtils.getUserFullNameFromUser(user);
        assertEquals(expected, fullname);
    }

    @Test
    public void getUserFullNameFromUserTestCivNull() {
        String expected = PRENOM + " " + NOM;
        User user = createUser(MATRICULE, PRENOM, NOM, NOM, NOM, null);
        String fullname = utilisateursUtils.getUserFullNameFromUser(user);
        assertEquals(expected, fullname);
    }

    @Test
    public void getUserFullNameFromUserTestNomNull() {
        String expected = "M. " + PRENOM + " ";
        User user = createUser(MATRICULE, PRENOM, null, null, null, Civilite.MONSIEUR);
        String fullname = utilisateursUtils.getUserFullNameFromUser(user);
        assertEquals(expected, fullname);
    }

}

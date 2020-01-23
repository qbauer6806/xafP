package mc.gouv.xaf.back.service.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import mc.gouv.logon.apiclient.RestException;
import mc.gouv.logon.shared.User;
import mc.gouv.logon.shared.User.Civilite;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;

@RunWith(MockitoJUnitRunner.class)
public class UtilisateursUtilsTest {
	
	// Données de l'utilisateur 1
	private User user_monsieur;
	private static final String MATRICULE = "1";
	private static final String PRENOM = "Toto";
	private static final String NOM = "Titi";
	
	// Données de l'utilisateur 2
	private User user_dame;
	private static final String MATRICULE_DAME = "2";
	private static final String PRENOM_DAME = "Ma";
	private static final String NOM_DAME = "Dame";
	private static final String NOM_NAISSANCE = "Naissance";
	
	// Données fausses
	private static final String MAUVAIS_MATRICULE = "0";
	
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
		Mockito.when(utilisateursCache.get(MATRICULE)).thenReturn(user_monsieur);
		Mockito.when(utilisateursCache.get(MAUVAIS_MATRICULE)).thenReturn(null);
	}

	@Test
	public void getUserNameFromIDTestBonUtilisateur() {
		String expected = PRENOM + " " + NOM;
		
		try {
			String nomPrenom = utilisateursUtils.getUserNameFromID(MATRICULE);
			assertEquals(expected, nomPrenom);
		} catch (RestException e) {
			e.printStackTrace();
			fail("L'exception RestException n'était pas attendu");
		}
	}
	
	@Test
	public void getUserNameFromIDTestMauvaisMatricule() {
		try {
			String nomPrenom = utilisateursUtils.getUserNameFromID(MAUVAIS_MATRICULE);
			assertNull(nomPrenom);
		} catch (RestException e) {
			e.printStackTrace();
			fail("L'exception RestException n'était pas attendu");
		}
	}
	

	@Test
	public void getUserFullNameFromUserTestBonUtilisateur() {
		String expected = "M. " + PRENOM + " " + NOM;
		String fullname = utilisateursUtils.getUserFullNameFromUser(user_monsieur);
		assertEquals(expected, fullname);
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
		String expected = NOM;
		User user = createUser(MATRICULE, null, NOM, NOM, NOM, null);
		String fullname = utilisateursUtils.getUserFullNameFromUser(user);
		assertEquals(expected, fullname);
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

package mc.gouv.xaf.back.util.users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.utils.UtilisateursUtils;
import mc.gouv.logon.apiclient.RestException;
import mc.gouv.logon.shared.User;
import mc.gouv.logon.shared.User.Civilite;

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
	public void getUserNameFromIDTest_bon_utilisateur() {
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
	public void getUserNameFromIDTest_mauvais_matricule() {
		try {
			String nomPrenom = utilisateursUtils.getUserNameFromID(MAUVAIS_MATRICULE);
			assertNull(nomPrenom);
		} catch (RestException e) {
			e.printStackTrace();
			fail("L'exception RestException n'était pas attendu");
		}
	}
	

	@Test
	public void getUserFullNameFromUserTest_bon_utilisateur() {
		String expected = "M. " + PRENOM + " " + NOM;
		String fullname = utilisateursUtils.getUserFullNameFromUser(user_monsieur);
		assertEquals(expected, fullname);
	}
	
	@Test
	public void getUserFullNameFromUserTest_jeune_fille() {
		String expected = "Mme. " + PRENOM_DAME + " " + NOM_NAISSANCE;
		String fullname = utilisateursUtils.getUserFullNameFromUser(user_dame);
		assertEquals(expected, fullname);
	}
	
	@Test
	public void getUserFullNameFromUserTest_mariee() {
		String expected = "Mme. " + PRENOM_DAME + " " + NOM_DAME;
		user_dame.setNomUsage(NOM_DAME);
		String fullname = utilisateursUtils.getUserFullNameFromUser(user_dame);
		assertEquals(expected, fullname);
	}

	@Test
	public void getUserFullNameFromUserTest_null() {
		String expected = "";
		User user = createUser(MATRICULE, null, null, null, null, null);
		String fullname = utilisateursUtils.getUserFullNameFromUser(user);
		assertEquals(expected, fullname);
	}

	@Test
	public void getUserFullNameFromUserTest_nomSeulement() {
		String expected = NOM;
		User user = createUser(MATRICULE, null, NOM, NOM, NOM, null);
		String fullname = utilisateursUtils.getUserFullNameFromUser(user);
		assertEquals(expected, fullname);
	}

	@Test
	public void getUserFullNameFromUserTest_prenomSeulement() {
		String expected = PRENOM + " ";
		User user = createUser(MATRICULE, PRENOM, null, null, null, null);
		String fullname = utilisateursUtils.getUserFullNameFromUser(user);
		assertEquals(expected, fullname);
	}

	@Test
	public void getUserFullNameFromUserTest_civSeulement() {
		String expected = "M. ";
		User user = createUser(MATRICULE, null, null, null, null, Civilite.MONSIEUR);
		String fullname = utilisateursUtils.getUserFullNameFromUser(user);
		assertEquals(expected, fullname);
	}

	@Test
	public void getUserFullNameFromUserTest_prenomNull() {
		String expected = "M. " + NOM;
		User user = createUser(MATRICULE, null, NOM, NOM, NOM, Civilite.MONSIEUR);
		String fullname = utilisateursUtils.getUserFullNameFromUser(user);
		assertEquals(expected, fullname);
	}

	@Test
	public void getUserFullNameFromUserTest_civNull() {
		String expected = PRENOM + " " + NOM;
		User user = createUser(MATRICULE, PRENOM, NOM, NOM, NOM, null);
		String fullname = utilisateursUtils.getUserFullNameFromUser(user);
		assertEquals(expected, fullname);
	}

	@Test
	public void getUserFullNameFromUserTest_nomNull() {
		String expected = "M. " + PRENOM + " ";
		User user = createUser(MATRICULE, PRENOM, null, null, null, Civilite.MONSIEUR);
		String fullname = utilisateursUtils.getUserFullNameFromUser(user);
		assertEquals(expected, fullname);
	}

}

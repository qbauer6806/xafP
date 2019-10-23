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
	private User user;
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
	
	@Before
	public void setUp() {
		user = new User();
		user.resetData();
		user.setMatricule(MATRICULE);
		user.setPrenom(PRENOM);
		user.setNomAffichage(NOM);
		user.setNomUsage(NOM);
		user.setNomNaissance(NOM);
		user.setCivilite(Civilite.MONSIEUR);
		
		user_dame = new User();
		user_dame.resetData();
		user_dame.setMatricule(MATRICULE_DAME);
		user_dame.setPrenom(PRENOM_DAME);
		user_dame.setNomAffichage(NOM_DAME);
		user_dame.setNomNaissance(NOM_NAISSANCE);
		user_dame.setCivilite(Civilite.MADAME);
		
		Mockito.when(utilisateursCache.get(MATRICULE)).thenReturn(user);
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
		String fullname = utilisateursUtils.getUserFullNameFromUser(user);
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

}

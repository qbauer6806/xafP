package mc.gouv.xaf.front.controller.util;

import mc.gouv.xaf.front.dto.FileUploadCompteurDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.FileControllerUtils;
import mc.gouv.xaf.front.util.FrontControllerPropertiesCache;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.el.PropertyNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileControllerUtilsTest {

    private static final String EXTENSIONS_WHITELIST = "EXTENSIONS_WHITELIST";
    private static final String MAX_TAILLE_FICHIER = "MAX_TAILLE_FICHIER";

    @Mock
    private FrontControllerPropertiesCache frontControllerPropertiesCache;
    @Mock
    private FrontGouvPropertiesResolver frontGouvPropertiesResolver;
    @InjectMocks
    private FileControllerUtils fileControllerUtils;

    @Test
    void testExtensionsWhitelist() {
        PropertiesDTO extensionsProperty = mock(PropertiesDTO.class);
        when(extensionsProperty.getValue()).thenReturn("*.pdf,*.png, *.*, *., .*");
        when(frontControllerPropertiesCache.getFrontProperty(EXTENSIONS_WHITELIST)).thenReturn(extensionsProperty);

        List<String> extensionsWhitelist = fileControllerUtils.getExtensionsWhitelist();
        assertEquals(extensionsWhitelist, List.of("pdf", "png", "*", "", ".*"));
    }

    @Test
    void testEstExtensionWhitelist() {
        PropertiesDTO extensionsProperty = mock(PropertiesDTO.class);
        when(extensionsProperty.getValue()).thenReturn("*.doc, *.docx, *.rtf, *.pdf, *.jpg, *.jpeg, *.png, *.tif");
        when(frontControllerPropertiesCache.getFrontProperty(EXTENSIONS_WHITELIST)).thenReturn(extensionsProperty);

        assertTrue(fileControllerUtils.estExtensionDansWhitelist("carteidentite.pdf"));
        assertTrue(fileControllerUtils.estExtensionDansWhitelist("carteidentite.tif"));
        assertTrue(fileControllerUtils.estExtensionDansWhitelist(".pdf"));
        assertTrue(fileControllerUtils.estExtensionDansWhitelist("*.pdf"));

        assertFalse(fileControllerUtils.estExtensionDansWhitelist("carteidentite.pdaf"));
        assertFalse(fileControllerUtils.estExtensionDansWhitelist("carteidentite."));
        assertFalse(fileControllerUtils.estExtensionDansWhitelist("carteidentite"));
        assertFalse(fileControllerUtils.estExtensionDansWhitelist(
                "pdf")); // Fichier simplement nommé "pdf" pourrait être un fichier .txt déguisé!
        assertFalse(fileControllerUtils.estExtensionDansWhitelist(""));
        assertFalse(fileControllerUtils.estExtensionDansWhitelist(" "));
        assertFalse(fileControllerUtils.estExtensionDansWhitelist("."));
    }

    @Test
    void testExtensionWhiteListNonTrouvee() {
        when(frontControllerPropertiesCache.getFrontProperty(EXTENSIONS_WHITELIST)).thenReturn(null);
        assertTrue(fileControllerUtils.getExtensionsWhitelist().isEmpty());
    }

    @Test
    void testTailleFichierValide() {
        PropertiesDTO tailleMaxProperty = mock(PropertiesDTO.class);
        when(tailleMaxProperty.getValue()).thenReturn("3"); // 3 MB
        when(frontControllerPropertiesCache.getFrontProperty(MAX_TAILLE_FICHIER)).thenReturn(tailleMaxProperty);

        Part bigfile = mock(Part.class);
        when(bigfile.getSize()).thenReturn(4L * 1_000_000L); // 4 MB

        Part smallfile = mock(Part.class);
        when(smallfile.getSize()).thenReturn(1L); // 1 byte

        Part equalfile = mock(Part.class);
        when(equalfile.getSize()).thenReturn(3L * 1_000_000L); // 3 MB

        assertFalse(fileControllerUtils.tailleFichierValide(bigfile));
        assertTrue(fileControllerUtils.tailleFichierValide(smallfile));
        assertTrue(fileControllerUtils.tailleFichierValide(equalfile));
    }

    @Test
    void testTailleFichierProprtyNotFoundException() {
        when(frontControllerPropertiesCache.getFrontProperty(MAX_TAILLE_FICHIER)).thenReturn(null);

        assertThrows(PropertyNotFoundException.class, () -> fileControllerUtils.tailleFichierValide(null));
    }

    private static Stream<Arguments> testLimiteUploadAtteinte() {
        return Stream.of(
                // Upload fait <= la limite d'intervale
                Arguments.of(100, 2, 0, 1, false), Arguments.of(100, 2, -100, 1, false),
                Arguments.of(100, 2, -100, 3, false),

                // Upload fait >= la limite d'intervale
                Arguments.of(100, 2, 0, 1, false), Arguments.of(100, 2, 100, 1, false),
                Arguments.of(100, 2, 101, 1, false),

                Arguments.of(100, 2, 0, 2, true), Arguments.of(100, 2, 100, 2, true),
                Arguments.of(100, 2, 101, 2, true),

                Arguments.of(100, 2, 0, 3, true), Arguments.of(100, 2, 100, 3, true),
                Arguments.of(100, 2, 101, 3, true));
    }

    @ParameterizedTest
    @MethodSource("testLimiteUploadAtteinte")
    void testLimiteUploadAtteinte(int tempsParIntervalle, int maxUploadParIntervalle, int tempsIntervaleActuel,
            int maxUploadActuel, boolean conditionAttendue) {
        @SuppressWarnings("unchecked") // À cause du mock d'un objet générique
        Map<HttpSession, FileUploadCompteurDTO> usagersFileUploadCompteurs = mock(HashMap.class);
        HttpSession session = mock(HttpSession.class);
        FileUploadCompteurDTO compteurUpload = mock(FileUploadCompteurDTO.class);

        when(compteurUpload.getDatePremierUpload()).thenReturn(
                LocalDateTime.now().plus(tempsIntervaleActuel, ChronoUnit.MILLIS));
        when(compteurUpload.getCompteur()).thenReturn(maxUploadActuel);
        when(usagersFileUploadCompteurs.get(session)).thenReturn(compteurUpload);

        when(frontGouvPropertiesResolver.getTempsIntervalleUpload()).thenReturn(String.valueOf(tempsParIntervalle));
        when(frontGouvPropertiesResolver.getMaxUploadParIntervalle()).thenReturn(
                String.valueOf(maxUploadParIntervalle));

        boolean conditionActuelle = fileControllerUtils.limiteUploadAtteinte(usagersFileUploadCompteurs, session);
        assertEquals(conditionActuelle, conditionAttendue);
    }

    private static Stream<Arguments> testSupprimeCompteurSiDepasse() {
        return Stream.of(
                // Upload fait <= la limite d'intervale
                Arguments.of(100, 2, 100, 1, false), Arguments.of(100, 2, -101, 1, true),

                // Upload fait >= à la limite d'intervale
                Arguments.of(100, 2, 0, 1, false), Arguments.of(100, 2, 101, 1, false));
    }

    @ParameterizedTest
    @MethodSource("testSupprimeCompteurSiDepasse")
    void testSupprimeCompteurSiDepasse(int tempsParIntervalle, int maxUploadParIntervalle, int tempsIntervaleActuel,
            int maxUploadActuel, boolean verifieSupprime) {
        @SuppressWarnings("unchecked") // À cause du mock d'un objet générique
        Map<HttpSession, FileUploadCompteurDTO> usagersFileUploadCompteurs = mock(HashMap.class);
        HttpSession session = mock(HttpSession.class);
        FileUploadCompteurDTO compteurUpload = mock(FileUploadCompteurDTO.class);

        when(compteurUpload.getDatePremierUpload()).thenReturn(
                LocalDateTime.now().plus(tempsIntervaleActuel, ChronoUnit.MILLIS));
        when(compteurUpload.getCompteur()).thenReturn(maxUploadActuel);
        when(usagersFileUploadCompteurs.get(session)).thenReturn(compteurUpload);
        when(frontGouvPropertiesResolver.getTempsIntervalleUpload()).thenReturn(String.valueOf(tempsParIntervalle));
        when(frontGouvPropertiesResolver.getMaxUploadParIntervalle()).thenReturn(
                String.valueOf(maxUploadParIntervalle));

        fileControllerUtils.limiteUploadAtteinte(usagersFileUploadCompteurs, session);

        if (verifieSupprime) {
            verify(usagersFileUploadCompteurs).remove(session);
        } else {
            verify(usagersFileUploadCompteurs, never()).remove(session);
        }
    }

    @Test
    void testReinitialierSessionsInutilisees() {
        HttpSession session1 = mock(HttpSession.class);
        HttpSession session2 = mock(HttpSession.class);
        HttpSession session3 = mock(HttpSession.class);

        FileUploadCompteurDTO compteur1 = new FileUploadCompteurDTO();
        compteur1.setDatePremierUpload(LocalDateTime.now().minusMinutes(100));

        FileUploadCompteurDTO compteur2 = new FileUploadCompteurDTO();
        compteur2.setDatePremierUpload(LocalDateTime.now());

        FileUploadCompteurDTO compteur3 = new FileUploadCompteurDTO();
        compteur3.setDatePremierUpload(LocalDateTime.now().plusMinutes(100));

        Map<HttpSession, FileUploadCompteurDTO> usagersFileUploadCompteurs = new HashMap<>(
                Map.of(session1, compteur1, session2, compteur2, session3, compteur3));

        when(frontGouvPropertiesResolver.getTempsIntervalleUpload()).thenReturn("100");

        fileControllerUtils.reinitialierSessionsInutilisees(usagersFileUploadCompteurs);

        assertEquals(2, usagersFileUploadCompteurs.size());

        compteur1 = usagersFileUploadCompteurs.get(session1);
        compteur2 = usagersFileUploadCompteurs.get(session2);
        compteur3 = usagersFileUploadCompteurs.get(session3);

        assertNull(compteur1);
        assertNotNull(compteur2);
        assertNotNull(compteur3);
    }
}

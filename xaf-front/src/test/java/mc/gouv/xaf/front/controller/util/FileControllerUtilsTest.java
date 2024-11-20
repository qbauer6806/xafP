package mc.gouv.xaf.front.controller.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.el.PropertyNotFoundException;
import jakarta.servlet.http.Part;
import java.time.LocalDateTime;
import java.util.List;
import mc.gouv.xaf.front.dto.FileUploadCompteurDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.FileControllerUtils;
import mc.gouv.xaf.front.util.FrontControllerPropertiesCache;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @BeforeEach
    void setUp() {
        fileControllerUtils.clearUsagerFileUploadCompteurs(); // Assure un état propre au démarrage de chaque test
    }

    @Test
    void testExtensionsWhitelist() {
        PropertiesDTO extensionsProperty = mock(PropertiesDTO.class);
        when(extensionsProperty.getValue()).thenReturn("*.pdf,*.png,*.jpg,*.jpeg");
        when(frontControllerPropertiesCache.getFrontProperty(EXTENSIONS_WHITELIST)).thenReturn(extensionsProperty);

        List<String> expected = List.of("pdf", "png", "jpg", "jpeg");
        List<String> actual = fileControllerUtils.getExtensionsWhitelist();
        assertEquals(expected, actual);
    }

    @Test
    void testEstExtensionWhitelist() {
        PropertiesDTO extensionsProperty = mock(PropertiesDTO.class);
        when(extensionsProperty.getValue()).thenReturn("*.doc, *.docx, *.rtf, *.pdf, *.jpg, *.jpeg, *.png, *.tif");
        when(frontControllerPropertiesCache.getFrontProperty(EXTENSIONS_WHITELIST)).thenReturn(extensionsProperty);

        assertTrue(fileControllerUtils.estExtensionDansWhitelist("document.pdf"));
        assertFalse(fileControllerUtils.estExtensionDansWhitelist("document.txt"));
        assertFalse(fileControllerUtils.estExtensionDansWhitelist("document"));
    }

    @Test
    void testTailleFichierValide() {
        PropertiesDTO tailleMaxProperty = mock(PropertiesDTO.class);
        when(tailleMaxProperty.getValue()).thenReturn("3"); // 3 MB
        when(frontControllerPropertiesCache.getFrontProperty(MAX_TAILLE_FICHIER)).thenReturn(tailleMaxProperty);

        Part bigfile = mock(Part.class);
        when(bigfile.getSize()).thenReturn(4L * 1024 * 1024); // Larger than 3 MB
        assertFalse(fileControllerUtils.tailleFichierValide(bigfile));

        Part smallfile = mock(Part.class);
        when(smallfile.getSize()).thenReturn(2L * 1024 * 1024); // Smaller than 3 MB
        assertTrue(fileControllerUtils.tailleFichierValide(smallfile));
    }

    @Test
    void testTailleFichierPropertyNotFoundException() {
        when(frontControllerPropertiesCache.getFrontProperty(MAX_TAILLE_FICHIER)).thenReturn(null);
        assertThrows(PropertyNotFoundException.class, () -> fileControllerUtils.tailleFichierValide(mock(Part.class)));
    }

    @Test
    void testLimiteUploadAtteinte() {
        FileUploadCompteurDTO compteurUpload = new FileUploadCompteurDTO();
        compteurUpload.setCompteur(5);
        compteurUpload.setDatePremierUpload(LocalDateTime.now().minusMinutes(1));

        Integer sessionId = 1;
        fileControllerUtils.addOrUpdateUsagerFileUploadCompteur(sessionId, compteurUpload);

        when(frontGouvPropertiesResolver.getTempsIntervalleUpload()).thenReturn("300000"); // 5 minutes in milliseconds
        when(frontGouvPropertiesResolver.getMaxUploadParIntervalle()).thenReturn("3");

        assertTrue(fileControllerUtils.limiteUploadAtteinte(sessionId));

        compteurUpload.setCompteur(1);
        fileControllerUtils.addOrUpdateUsagerFileUploadCompteur(sessionId, compteurUpload);
        assertFalse(fileControllerUtils.limiteUploadAtteinte(sessionId));
    }
}

package mc.gouv.xaf.back.dsp.service.itg.resid.impl;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidUsagerNpdhlDTO;
import mc.gouv.xaf.back.dsp.exception.ResidHttpResponseException;
import mc.gouv.xaf.back.dsp.service.itg.resid.ResidApiService;
import mc.gouv.xaf.back.dsp.service.itg.resid.ResidInitialDemandeMapper;
import mc.gouv.xaf.back.dsp.service.itg.resid.ResidPropertiesResolver;
import mc.gouv.xaf.shared.enums.SourceDonneesEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResidInitialDemandeServiceImplTest {

    @Mock
    private ResidApiService residApiService;
    @Mock
    private ResidPropertiesResolver residPropertiesResolver;
    @Spy
    private ResidInitialDemandeMapper residInitialDemandeMapper;

    @InjectMocks
    private ResidInitialDemandeServiceImpl residInitialDemandeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetInitialDemandeWhenParamIsEmptyThenReturnNOK()
            throws ParseException, JacksonException, ResidHttpResponseException {

        // Call the method to test
        JsonNode result = residInitialDemandeService.getInitialDemande(1, new HashMap<>());

        // Assert the result or perform other necessary validations
        assertEquals(1, result.size());
        assertEquals("NOK", result.get("statut").asString());
        verify(residApiService, Mockito.never()).getUsagerDln1f(any(), any(), any(), any());
    }

    @Test
    void testGetInitialDemandeWhenTokenIsEmptyThenThrowException() throws ParseException, JacksonException {

        when(residPropertiesResolver.getResidApiJwt()).thenReturn(null);

        assertThrows(ResidHttpResponseException.class, () -> {
            // When
            residInitialDemandeService.getInitialDemande(1, getParams());
        });

        verify(residPropertiesResolver, Mockito.never()).getResidApiUrl();
        verify(residApiService, Mockito.never()).getUsagerDln1f(any(), any(), any(), any());
        verify(residInitialDemandeMapper, Mockito.never()).mapperDonneesResid(any(), any(), any());
    }

    @Test
    void testGetInitialDemandeWhenUrlResidIsEmptyThenThrowException() throws ParseException, JacksonException {

        when(residPropertiesResolver.getResidApiJwt()).thenReturn("mocked_jwt");
        when(residPropertiesResolver.getResidApiUrl()).thenReturn(null);

        assertThrows(ResidHttpResponseException.class, () -> {
            // When
            residInitialDemandeService.getInitialDemande(1, getParams());
        });

        verify(residApiService, Mockito.never()).getUsagerDln1f(any(), any(), any(), any());
        verify(residInitialDemandeMapper, Mockito.never()).mapperDonneesResid(any(), any(), any());
    }

    @Test
    void testGetInitialDemandeWhenResidReturnNullThenReturnNOK()
            throws ParseException, JacksonException, ResidHttpResponseException {
        when(residPropertiesResolver.getResidApiJwt()).thenReturn("mocked_jwt");
        when(residPropertiesResolver.getResidApiUrl()).thenReturn("mocked_url");
        // Mocking ResidApiService response
        when(residApiService.getUsagerDln1f(any(), any(), any(), any())).thenReturn(null);

        // Prepare parameters for the method
        Map<String, String[]> params = getParams();

        // Call the method to test
        JsonNode result = residInitialDemandeService.getInitialDemande(1, params);

        // Assert the result or perform other necessary validations
        assertEquals(1, result.size());
        assertEquals("NOK", result.get("statut").asString());
        verify(residInitialDemandeMapper, Mockito.never()).mapperDonneesResid(any(), any(), any());
    }

    @Test
    void testGetInitialDemandeWhenResidIsOKThenReturnStatusOK()
            throws ParseException, JacksonException, ResidHttpResponseException {
        // Mocking external services
        when(residPropertiesResolver.getResidApiJwt()).thenReturn("mocked_jwt");
        when(residPropertiesResolver.getResidApiUrl()).thenReturn("mocked_url");
        // Mocking ResidApiService response
        when(residApiService.getUsagerDln1f(any(), any(), any(), any())).thenReturn(new ResidUsagerNpdhlDTO());

        // Prepare parameters for the method
        Map<String, String[]> params = getParams();

        // Call the method to test
        JsonNode result = residInitialDemandeService.getInitialDemande(1, params);

        // Assert the result or perform other necessary validations
        assertEquals(SourceDonneesEnum.RESID.name(), result.get("source").asString());
        assertEquals("OK", result.get("statut").asString());
    }

    private Map<String, String[]> getParams() {
        Map<String, String[]> params = new HashMap<>();
        params.put(ResidInitialDemandeServiceImpl.DONNEES_EXTERNES_MCONNECT_BIRTHDATE,
                new String[] { "1990-01-01T00:00:00+01:00" });
        params.put(ResidInitialDemandeServiceImpl.DONNEES_EXTERNES_MCONNECT_FAMILYNAME, new String[] { "FAMILYNAME" });

        params.put(ResidInitialDemandeServiceImpl.DONNEES_EXTERNES_MCONNECT_BIRTHNAME, new String[] { "BIRTHNAME" });
        params.put(ResidInitialDemandeServiceImpl.DONNEES_EXTERNES_MCONNECT_GIVENNAME, new String[] { "GIVENNAME" });
        params.put(ResidInitialDemandeServiceImpl.DONNEES_EXTERNES_MCONNECT_BIRTHPLACECITY,
                new String[] { "BIRTHPLACECITY" });
        params.put(ResidInitialDemandeServiceImpl.DONNEES_EXTERNES_MCONNECT_BIRTHPLACECOUNTRY,
                new String[] { "BIRTHPLACECOUNTRY" });
        params.put(ResidInitialDemandeServiceImpl.DONNEES_EXTERNES_USAGER_INFO_EMAIL, new String[] { "EMAIL" });
        params.put(ResidInitialDemandeServiceImpl.DONNEES_EXTERNES_USAGER_INFO_TITRE, new String[] { "TITRE" });

        return params;
    }
}

package mc.gouv.xaf.back.service.scheduling;

import mc.gouv.xaf.back.data.dao.AccessRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionException;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpirationDocHolderConsentSchedulingJobTest {
    @Mock
    GouvPropertiesResolver gouvPropertiesResolver;
    @Mock
    AccessRepository accessRepository;
    @Mock
    PropertiesService propertiesService;

    @InjectMocks
    ExpirationDocHolderConsentSchedulingJob job;

    static Date today = Date.from(Instant.now().atZone(ZoneId.of("Europe/Monaco")).toInstant());
    static Date expiredDate = Date.from(Instant.now().atZone(ZoneId.of("Europe/Monaco")).minusYears(1).minusMonths(1).toInstant());
    static Date notExpiredDate = Date.from(Instant.now().atZone(ZoneId.of("Europe/Monaco")).minusYears(1).toInstant());
    static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    static final SimpleDateFormat dateFormat = new SimpleDateFormat(JSON_DATE_FORMAT);
    static final String XAF_PORTE_DOCUMENT_ACTIF = "XAF_PORTE_DOCUMENT_ACTIF";

    private static Stream<Arguments> accesses() {
        return Stream.of(
                Arguments.of(accessWithoutData(), 0),
                Arguments.of(accessWithConsentAllFields(true, today), 0),
                Arguments.of(accessWithConsentAllFields(true, notExpiredDate), 0),
                Arguments.of(accessWithConsentAllFields(true, expiredDate), 1),
                Arguments.of(accessWithConsentAllFields(false, today), 0),
                Arguments.of(accessWithConsentAllFields(false, notExpiredDate), 0),
                Arguments.of(accessWithConsentAllFields(false, expiredDate), 0)
        );
    }

    @ParameterizedTest
    @MethodSource("accesses")
    void testExecute(AccessBO access, int numberOfSave) throws JobExecutionException {

        PropertiesDTO xafPorteDocumentActif = new PropertiesDTO(XAF_PORTE_DOCUMENT_ACTIF, "true");
        when(propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_PORTE_DOCUMENT_ACTIF)).thenReturn(xafPorteDocumentActif);
        when(gouvPropertiesResolver.getDemarcheId()).thenReturn("POCTS");
        when(accessRepository.getByDemarcheIdAndActive(anyString(), anyBoolean())).thenReturn(List.of(access));

        job.execute(null);

        verify(accessRepository, times(numberOfSave)).save(access);

    }

    static AccessBO accessWithoutData() {
        AccessBO access = new AccessBO();
        access.setPkAccess(123);
        access.setContenu("{\"CGU\":true}");

        return access;
    }

    static AccessBO accessWithConsentAllFields(boolean consenting, Date dateCreation) {
        AccessBO access = new AccessBO();
        String fmtDate = dateFormat.format(dateCreation);
        access.setPkAccess(123);
        access.setContenu("{\"CGU\":true, \"docholderConsent\":{\"consenting\":" + consenting + ", \"dateCreation\":\"" + fmtDate + "\"}}");

        return access;
    }
}

#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.service.afimpl;

import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.dem.service.DemandesService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RunWith(MockitoJUnitRunner.class)
public class ExcelExportModelProviderImplTest {

    private final static String PLAIN_START_DATE = "10/11/2020";

    private final static String PLAIN_END_DATE = "10/12/2020";

    private final static LocalDate START_DATE = LocalDate.of(2020, 11, 10);

    private final static LocalDateTime END_DATE = LocalDateTime.of(2020, 12, 10, 23, 59,59);

    private final static String DEMARCHE = "demarche";

    @InjectMocks
    private ExcelExportModelProviderImpl excelExportModelProvider;

    @Mock
    private DemandesService demandesService;

    @Mock
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Before
    public void setUp() {
        Mockito.when(gouvPropertiesResolver.getDemarcheId()).thenReturn(DEMARCHE);
    }

    @Test
    public void retrieveFilteredByDate_null_dates_test() {
        excelExportModelProvider.retrieveDemandesFilteredByDate(null, null);
        Mockito.verify(demandesService).getAllDemandesFilteredByDate(DEMARCHE, null, null);
    }


    @Test
    public void retrieveFilteredByDate_no_dates_test() {
        excelExportModelProvider.retrieveDemandesFilteredByDate("", "");
        Mockito.verify(demandesService).getAllDemandesFilteredByDate(DEMARCHE, null, null);
    }

    @Test
    public void retrieveFilteredByDate_all_dates_test() {
        excelExportModelProvider.retrieveDemandesFilteredByDate(PLAIN_START_DATE, PLAIN_END_DATE);

        Date start_date = Date.from(START_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end_date = Date.from(END_DATE.atZone(ZoneId.systemDefault()).toInstant());
        Mockito.verify(demandesService).getAllDemandesFilteredByDate(DEMARCHE, start_date, end_date);
    }

    @Test
    public void retrieveFilteredByDate_start_date_test() {
        excelExportModelProvider.retrieveDemandesFilteredByDate(PLAIN_START_DATE, null);

        Date start_date = Date.from(START_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Mockito.verify(demandesService).getAllDemandesFilteredByDate(DEMARCHE, start_date, null);
    }

    @Test
    public void retrieveFilteredByDate_end_date_test() {
        excelExportModelProvider.retrieveDemandesFilteredByDate(null, PLAIN_END_DATE);

        Date end_date = Date.from(END_DATE.atZone(ZoneId.systemDefault()).toInstant());
        Mockito.verify(demandesService).getAllDemandesFilteredByDate(DEMARCHE, null, end_date);
    }
}
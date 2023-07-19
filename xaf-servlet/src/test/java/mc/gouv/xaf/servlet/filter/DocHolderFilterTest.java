package mc.gouv.xaf.servlet.filter;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocHolderFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @InjectMocks
    private DocHolderFilter docHolderFilter;
    @Mock
    private ServletOutputStream servletOutputStream;

    MockedStatic<AppFactoryServletUtils> servletUtilsMocked;

    @BeforeEach
    public void setup() throws IOException {
        servletUtilsMocked = mockStatic(AppFactoryServletUtils.class, CALLS_REAL_METHODS);
        when(response.getOutputStream()).thenReturn(servletOutputStream);
    }

    @AfterEach
    public void shutdown() {
        servletUtilsMocked.close();
    }

    @Test
    void testDocHolderEnabled() throws ServletException, IOException {
        UsagerInfosDTO usagerInfosDTO = new UsagerInfosDTO();
        servletUtilsMocked.when(() -> AppFactoryServletUtils.getLoggedUser(any())).thenReturn(usagerInfosDTO);

        docHolderFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(eq(request), eq(response));
    }

    @Test
    void testDocHolderDisabled() throws ServletException, IOException {
        UsagerInfosDTO usagerInfosDTO = new UsagerInfosDTO();
        servletUtilsMocked.when(() -> AppFactoryServletUtils.getLoggedUser(any())).thenReturn(usagerInfosDTO);
        //TODO :  AfServletGouvPropertiesResolver.isPorteDocEnabled();
        docHolderFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(eq(request), eq(response));
    }

    @Test
    void testUserNotConnected() throws ServletException, IOException {
        servletUtilsMocked.when(() -> AppFactoryServletUtils.getLoggedUser(any())).thenReturn(null);

        when(request.getPathInfo()).thenReturn("PATH");

        docHolderFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpStatus.SC_UNAUTHORIZED);
    }
}

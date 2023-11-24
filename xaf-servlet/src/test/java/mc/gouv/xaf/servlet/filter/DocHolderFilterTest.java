package mc.gouv.xaf.servlet.filter;

import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import mc.gouv.xaf.servlet.dto.KeycloakTokenInfo;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;

@ExtendWith(MockitoExtension.class)
class DocHolderFilterTest {

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

    MockedStatic<AfServletGouvPropertiesResolver> propertiesResolver;

    @BeforeEach
    void setup() {
        servletUtilsMocked = mockStatic(AppFactoryServletUtils.class, CALLS_REAL_METHODS);
        propertiesResolver = mockStatic(AfServletGouvPropertiesResolver.class, CALLS_REAL_METHODS);
        servletOutputStream = mock(ServletOutputStream.class);
    }

    @AfterEach
    void shutdown() {
        servletUtilsMocked.close();
        propertiesResolver.close();
        docHolderFilter.destroy();
    }

    @Test
    void testDocHolderEnabledUserLogged() throws ServletException, IOException {
        UsagerInfosDTO usagerInfosDTO = mock(UsagerInfosDTO.class);
        KeycloakTokenInfo tokenInfo = mock(KeycloakTokenInfo.class);
        when(usagerInfosDTO.getTokenInfo()).thenReturn(tokenInfo);

        propertiesResolver.when(AfServletGouvPropertiesResolver::isPorteDocEnabled).thenReturn("true");

        servletUtilsMocked.when(() -> AppFactoryServletUtils.getLoggedUser(any())).thenReturn(usagerInfosDTO);
        docHolderFilter.init(mock(FilterConfig.class));
        docHolderFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDocHolderEnabledUserNotLogged() throws ServletException, IOException {
        propertiesResolver.when(AfServletGouvPropertiesResolver::isPorteDocEnabled).thenReturn("true");
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        docHolderFilter.init(mock(FilterConfig.class));
        docHolderFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void testDocHolderEnabledUserLoggedNoTokenInfo() throws ServletException, IOException {
        UsagerInfosDTO usagerInfosDTO = mock(UsagerInfosDTO.class);
        when(usagerInfosDTO.getTokenInfo()).thenReturn(null);

        servletUtilsMocked.when(() -> AppFactoryServletUtils.getLoggedUser(eq(request))).thenReturn(usagerInfosDTO);
        propertiesResolver.when(AfServletGouvPropertiesResolver::isPorteDocEnabled).thenReturn("true");
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        docHolderFilter.init(mock(FilterConfig.class));
        docHolderFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void testDocHolderDisabled() throws ServletException, IOException {
        propertiesResolver.when(AfServletGouvPropertiesResolver::isPorteDocEnabled).thenReturn("false");

        docHolderFilter.init(mock(FilterConfig.class));
        docHolderFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void testDocHolderDisabledUserNotLogged() throws ServletException, IOException {
        servletUtilsMocked.when(() -> AppFactoryServletUtils.getLoggedUser(eq(request))).thenReturn(null);
        propertiesResolver.when(AfServletGouvPropertiesResolver::isPorteDocEnabled).thenReturn("false");

        docHolderFilter.init(mock(FilterConfig.class));
        docHolderFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpStatus.SC_FORBIDDEN);
    }
}

package mc.gouv.xaf.front.controller.filter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import mc.gouv.xaf.front.dto.KeycloakTokenInfo;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.filter.DocHolderFilter;
import mc.gouv.xaf.front.util.FrontControllerPropertiesCache;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocHolderFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @Mock
    private XafFrontserverUtils xafFrontserverUtils;
    @Mock
    private FrontControllerPropertiesCache frontControllerPropertiesCache;
    @InjectMocks
    private DocHolderFilter docHolderFilter;

    private static final String XAF_PORTE_DOCUMENT_ACTIF = "XAF_PORTE_DOCUMENT_ACTIF";

    @AfterEach
    void shutdown() {
        docHolderFilter.destroy();
    }

    @Test
    void testDocHolderEnabledUserLogged() throws ServletException, IOException {
        UsagerInfosDTO usagerInfosDTO = mock(UsagerInfosDTO.class);
        KeycloakTokenInfo tokenInfo = mock(KeycloakTokenInfo.class);
        when(usagerInfosDTO.getTokenInfo()).thenReturn(tokenInfo);

        when(frontControllerPropertiesCache.getFrontProperty(anyString())).thenReturn(
                new PropertiesDTO(XAF_PORTE_DOCUMENT_ACTIF, "true"));

        when(request.getServletPath()).thenReturn("/doc-holder");

        when(xafFrontserverUtils.getLoggedUser(any())).thenReturn(usagerInfosDTO);
        FilterConfig filterConfig = mock(FilterConfig.class);
        Enumeration mock = mock(Enumeration.class);
        when(filterConfig.getInitParameterNames()).thenReturn(mock);
        docHolderFilter.init(filterConfig);
        docHolderFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDocHolderEnabledUserNotLogged() throws ServletException, IOException {
        when(frontControllerPropertiesCache.getFrontProperty(anyString())).thenReturn(
                new PropertiesDTO(XAF_PORTE_DOCUMENT_ACTIF, "true"));

        when(request.getServletPath()).thenReturn("/doc-holder");

        FilterConfig filterConfig = mock(FilterConfig.class);
        Enumeration mock = mock(Enumeration.class);
        when(filterConfig.getInitParameterNames()).thenReturn(mock);
        docHolderFilter.init(filterConfig);
        docHolderFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void testDocHolderEnabledUserLoggedNoTokenInfo() throws ServletException, IOException {
        UsagerInfosDTO usagerInfosDTO = mock(UsagerInfosDTO.class);
        when(usagerInfosDTO.getTokenInfo()).thenReturn(null);

        when(xafFrontserverUtils.getLoggedUser(request)).thenReturn(usagerInfosDTO);

        when(frontControllerPropertiesCache.getFrontProperty(anyString())).thenReturn(
                new PropertiesDTO(XAF_PORTE_DOCUMENT_ACTIF, "true"));

        when(request.getServletPath()).thenReturn("/doc-holder");

        FilterConfig filterConfig = mock(FilterConfig.class);
        Enumeration mock = mock(Enumeration.class);
        when(filterConfig.getInitParameterNames()).thenReturn(mock);
        docHolderFilter.init(filterConfig);

        docHolderFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void testDocHolderDisabled() throws ServletException, IOException {
        when(frontControllerPropertiesCache.getFrontProperty(anyString())).thenReturn(
                new PropertiesDTO(XAF_PORTE_DOCUMENT_ACTIF, "false"));

        when(request.getServletPath()).thenReturn("/doc-holder");

        FilterConfig filterConfig = mock(FilterConfig.class);
        Enumeration mock = mock(Enumeration.class);
        when(filterConfig.getInitParameterNames()).thenReturn(mock);
        docHolderFilter.init(filterConfig);

        docHolderFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void testDocHolderDisabledUserNotLogged() throws ServletException, IOException {

        when(frontControllerPropertiesCache.getFrontProperty(anyString())).thenReturn(
                new PropertiesDTO(XAF_PORTE_DOCUMENT_ACTIF, "false"));

        when(request.getServletPath()).thenReturn("/doc-holder");

        FilterConfig filterConfig = mock(FilterConfig.class);
        Enumeration mock = mock(Enumeration.class);
        when(filterConfig.getInitParameterNames()).thenReturn(mock);
        docHolderFilter.init(filterConfig);

        docHolderFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpStatus.SC_FORBIDDEN);
    }
}


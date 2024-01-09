package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.servlet.dto.DocHolderFilePostDTO;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.servlet.util.FileServletUtils;
import org.apache.http.HttpStatus;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.DelegatingServletInputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Ignore
@ExtendWith(MockitoExtension.class)
class DocHolderServletsTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ServletOutputStream servletOutputStream;

    MockedStatic<AppFactoryServletUtils> servletUtilsMocked;
    MockedStatic<FileServletUtils> fileServletUtilsMocked;

    @BeforeEach
    void setup() throws IOException {
        // On utilise CALLS_REAL_METHOD ici. Cela sert à mocker par exemple la méthode getLoggedUser
        // mais aussi pouvoir continuer le test quand d'autres méthodes comme logAndSendError sont appelées
        // et ainsi vérifier les statusCode définis dans les responses des Servlets.
        servletUtilsMocked = mockStatic(AppFactoryServletUtils.class, CALLS_REAL_METHODS);
        fileServletUtilsMocked = mockStatic(FileServletUtils.class, CALLS_REAL_METHODS);

        when(response.getOutputStream()).thenReturn(servletOutputStream);
    }

    @AfterEach
    void shutdown() {
        // Important ! Sinon, la classe statique n'est pas remise à zéro entre les tests.
        // Dans le cas où vous devez vous en passer, déclarez le mock statique dans un bloc try-with-resource
        servletUtilsMocked.close();
        fileServletUtilsMocked.close();
    }

    @Test
    void failOnUserNotLoggedTest() throws ServletException, IOException {
        servletUtilsMocked.when(() -> AppFactoryServletUtils.getLoggedUser(any())).thenReturn(null);

        DocHolderTypedocServlet DocHolderTypedocServlet = new DocHolderTypedocServlet();
        DocHolderTypedocServlet.doGet(request, response);

        verify(response).setStatus(HttpStatus.SC_UNAUTHORIZED);
    }

    private static Stream<Arguments> emptyOrInvalidFileParameters() {
        return Stream.of(
                Arguments.of(null, null, null),
                Arguments.of(null, null, ""),
                Arguments.of(null, "", null),
                Arguments.of("", "", null),
                Arguments.of("", "  ", null),
                Arguments.of("", "", ""),
                Arguments.of("  ", "  ", "  ")
        );
    }

    @ParameterizedTest
    @MethodSource("emptyOrInvalidFileParameters")
    void failOnBadParameters(String url, String typedoc, String preferredName) throws ServletException, IOException {
        UsagerInfosDTO usagerInfosDTO = mock(UsagerInfosDTO.class);
        DocHolderFileServlet fileServlet = new DocHolderFileServlet();

        servletUtilsMocked.when(() -> AppFactoryServletUtils.getLoggedUser(any())).thenReturn(usagerInfosDTO);

        DocHolderFilePostDTO filePostDTO = new DocHolderFilePostDTO();
        filePostDTO.setUrl(url);
        filePostDTO.setTypedoc(typedoc);
        filePostDTO.setPreferredName(preferredName);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(filePostDTO);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
             DelegatingServletInputStream dsis = new DelegatingServletInputStream(bais)) {
            when(request.getInputStream()).thenReturn(dsis);

            fileServlet.doPost(request, response);

            verify(response).setStatus(HttpStatus.SC_BAD_REQUEST);
        }
    }
}

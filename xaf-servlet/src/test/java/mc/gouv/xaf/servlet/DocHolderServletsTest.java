package mc.gouv.xaf.servlet;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import org.apache.http.HttpStatus;
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

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DocHolderServletsTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ServletOutputStream servletOutputStream;

    MockedStatic<AppFactoryServletUtils> servletUtilsMocked;

    @BeforeEach
    void setup() throws IOException {
        // On utilise CALLS_REAL_METHOD ici. Cela sert à mocker par exemple la méthode getLoggedUser
        // mais aussi pouvoir continuer le test quand d'autres méthodes comme logAndSendError sont appelées
        // et ainsi vérifier les statusCode définis dans les responses des Servlets.
        servletUtilsMocked = mockStatic(AppFactoryServletUtils.class, CALLS_REAL_METHODS);

        when(response.getOutputStream()).thenReturn(servletOutputStream);
    }

    @AfterEach
    void shutdown() {
        // Important ! Sinon, la classe statique n'est pas remise à zéro entre les tests.
        // Dans le cas où vous devez vous en passer, déclarez le mock statique dans un bloc try-with-resource
        servletUtilsMocked.close();
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
                Arguments.of(null, null),
                Arguments.of(null, ""),
                Arguments.of("", null),
                Arguments.of("", ""),
                Arguments.of("", "  "),
                Arguments.of("  ", ""),
                Arguments.of("  ", "  ")
        );
    }

    @ParameterizedTest
    @MethodSource("emptyOrInvalidFileParameters")
    void failOnBadParameters(String typedoc, String preferedName) throws ServletException, IOException {
        UsagerInfosDTO usagerInfosDTO = mock(UsagerInfosDTO.class);
        DocHolderFileServlet fileServlet = new DocHolderFileServlet();

        servletUtilsMocked.when(() -> AppFactoryServletUtils.getLoggedUser(any())).thenReturn(usagerInfosDTO);

        when(request.getParameter("typedoc")).thenReturn(typedoc);
        when(request.getParameter("preferedName")).thenReturn(preferedName);

        fileServlet.doPost(request, response);

        verify(response).setStatus(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void failOnNoFileProvided() throws ServletException, IOException {
        UsagerInfosDTO usagerInfosDTO = mock(UsagerInfosDTO.class);
        servletUtilsMocked.when(() -> AppFactoryServletUtils.getLoggedUser(any())).thenReturn(usagerInfosDTO);

        when(request.getParameter("typedoc")).thenReturn("mockedTypeDoc");
        when(request.getParameter("preferedName")).thenReturn("monbeaufichier.txt");
        when(request.getParts()).thenReturn(Collections.emptyList());

        DocHolderFileServlet fileServlet = new DocHolderFileServlet();
        fileServlet.doPost(request, response);

        verify(response).setStatus(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void failOnTooManyFilesProvided() throws ServletException, IOException {
        UsagerInfosDTO usagerInfosDTO = mock(UsagerInfosDTO.class);
        servletUtilsMocked.when(() -> AppFactoryServletUtils.getLoggedUser(any())).thenReturn(usagerInfosDTO);

        when(request.getParameter("typedoc")).thenReturn("mockedTypeDoc");
        when(request.getParameter("preferedName")).thenReturn("monbeaufichier.txt");

        Part filePartA = mock(Part.class);
        Part filePartB = mock(Part.class);

        when(request.getParts()).thenReturn(List.of(filePartA, filePartB));

        DocHolderFileServlet fileServlet = new DocHolderFileServlet();
        fileServlet.doPost(request, response);

        verify(response).setStatus(HttpStatus.SC_BAD_REQUEST);
    }
}

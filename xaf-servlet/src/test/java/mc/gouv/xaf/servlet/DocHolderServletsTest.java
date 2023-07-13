package mc.gouv.xaf.servlet;

import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocHolderServletsTest {

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @Mock
    private RequestDispatcher requestDispatcher;

    @InjectMocks
    private DocHolderSearchServlet docHolderSearchServlet;

    @Test
    public void failOnUserNotLoggedTest() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        /*StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);*/
        //when(response.getWriter()).thenReturn(writer);

        MockedStatic<AppFactoryServletUtils> servletUtilsMocked = mockStatic(AppFactoryServletUtils.class);
        servletUtilsMocked.when(() -> AppFactoryServletUtils.getLoggedUser(any())).thenReturn(null);

        docHolderSearchServlet.doGet(request, response);

        Logger logger = LoggerFactory.getLogger(DocHolderSearchServlet.class);
        verify(logger, times(1)).error(SharedMessages.UTILISATEUR_NON_AUTORISE);
    }
}

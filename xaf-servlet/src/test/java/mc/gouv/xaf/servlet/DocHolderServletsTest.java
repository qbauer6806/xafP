package mc.gouv.xaf.servlet;

import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocHolderServletsTest {

    @InjectMocks
    private DocHolderSearchServlet docHolderSearchServlet;

    @Test
    public void failOnUserNotLoggedTest() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        try(MockedStatic<AppFactoryServletUtils> servletUtilsMocked = mockStatic(AppFactoryServletUtils.class)) {
            servletUtilsMocked.when(() -> AppFactoryServletUtils.getLoggedUser(any())).thenReturn(null);

            docHolderSearchServlet.doGet(request, response);
        }

        Logger logger = LoggerFactory.getLogger(DocHolderSearchServlet.class);
        verify(logger, times(1)).error(SharedMessages.UTILISATEUR_NON_AUTORISE);

        assertEquals(response.getStatus(), HttpStatus.SC_UNAUTHORIZED);
    }
}

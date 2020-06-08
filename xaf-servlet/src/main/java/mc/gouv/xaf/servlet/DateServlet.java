package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.servlet.dto.DateDTO;
import mc.gouv.xaf.servlet.enums.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class DateServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -4404357609117058597L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DateServlet.class);

    public HttpServletResponse doHttpMethod(HttpServletRequest request, HttpServletResponse response,
                                            HttpMethod httpMethod) throws IOException {
        response.setStatus(HttpStatus.SC_OK);
        ObjectMapper mapper = new ObjectMapper();
        String repJson = mapper.writeValueAsString(new DateDTO());

        response.setContentType("application/json");
        IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());

        return response;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /date doGet()");

        try {
            doHttpMethod(request, response, HttpMethod.GET);
        } catch (Exception e) {
            LOGGER.error("DateServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /date doGet()");
    }
}

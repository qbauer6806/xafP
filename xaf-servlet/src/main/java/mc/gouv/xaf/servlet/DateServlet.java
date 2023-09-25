package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.servlet.dto.DateDTO;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class DateServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -4404357609117058597L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DateServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /date doGet()");
        try {
            ObjectMapper mapper = new ObjectMapper();
            String repJson = mapper.writeValueAsString(new DateDTO());
            response.setContentType(MediaType.APPLICATION_JSON);
            IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
            response.setStatus(HttpStatus.SC_OK);
        } catch (IOException e) {
            LOGGER.error("DateServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        LOGGER.info("====================== Fin /date doGet()");
    }
}

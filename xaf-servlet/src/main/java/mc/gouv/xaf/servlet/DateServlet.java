package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.servlet.dto.DateDTO;
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

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.info("====================== /date doGet()");

        response.setStatus(HttpStatus.SC_OK);
        ObjectMapper mapper = new ObjectMapper();
        String repJson = mapper.writeValueAsString(new DateDTO());

        response.setContentType("application/json");
        IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());

        LOGGER.info("====================== Fin /date doGet()");
    }
}

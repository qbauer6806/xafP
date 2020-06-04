package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class PropertiesServlet extends AbstractAfServlet {

    private static final long serialVersionUID = 8231464687093443490L;

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.info("====================== /properties doGet()");

        LOGGER.info("Appel de la démarche afin de récupérer les propriétés FRONT ...");
        List<PropertiesDTO> properties = getAfApiClient().getFrontProperties();

        LOGGER.info("Ajout des properties du fichier frontserver.properties...");
        properties.addAll(AfServletGouvPropertiesResolver.getFrontProperties());

        response.setStatus(HttpStatus.SC_OK);
        ObjectMapper mapper = new ObjectMapper();
        String repJson = mapper.writeValueAsString(properties);

        response.setContentType("application/json");
        IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());

        LOGGER.info("====================== Fin /properties doGet()");
    }
}

package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.util.List;

public class PropertiesServlet extends AbstractAfServlet {

    private static final long serialVersionUID = 8231464687093443490L;

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /properties doGet()");
        LOGGER.info("Appel de la démarche afin de récupérer les propriétés FRONT ...");
        try {
            List<PropertiesDTO> properties = getAfApiClient().getFrontProperties();
            LOGGER.info("Ajout des properties du fichier frontserver.properties...");
            properties.addAll(AfServletGouvPropertiesResolver.getFrontProperties());
            ObjectMapper mapper = new ObjectMapper();
            String repJson = mapper.writeValueAsString(properties);
            response.setContentType(MediaType.APPLICATION_JSON);
            IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
            response.setStatus(HttpStatus.SC_OK);
        } catch (Exception e) {
            LOGGER.error("PropertiesServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            int codeStatut = getCodeErreur(e);
            response.setStatus(codeStatut);
        }
        LOGGER.info("====================== Fin /properties doGet()");
    }
}

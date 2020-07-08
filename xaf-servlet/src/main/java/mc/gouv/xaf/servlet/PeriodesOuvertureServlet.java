package mc.gouv.xaf.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;

/**
 * Servlet mettant à disposition le service /periodesouverture avec uniquement la méthode GET pour le front.
 * Cette servlet récupère le DemarcheID et appelle le WS dans le back-end générique.
 * 
 * @author qdeme
 *
 */
public class PeriodesOuvertureServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -7898768899143027088L;

    private static Logger LOGGER = LoggerFactory.getLogger(PeriodesOuvertureServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /periodesouverture doGet()");

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
            return;
        }
        
        LOGGER.info("Appel de la démarche afin de récupérer les périodes d'ouverture...");
        List<PeriodeOuvertureDTO> periodes = getAfApiClient().getPeriodesOuverture();
        
        response.setStatus(HttpStatus.SC_OK);
        ObjectMapper mapper = new ObjectMapper();
        String repJson = mapper.writeValueAsString(periodes);
        
        response.setContentType("application/json");
        IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());

        LOGGER.info("====================== Fin /periodesouverture doGet()");
    }
}

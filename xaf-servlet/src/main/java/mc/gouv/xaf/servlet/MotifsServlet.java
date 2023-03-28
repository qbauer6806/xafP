package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.MotifDTO;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Servlet mettant à disposition le service /motifs avec uniquement la méthode GET pour le front.
 * Cette servlet récupère le DemarcheID et appelle le WS dans le back-end générique.
 *
 * @author qdeme
 */
public class MotifsServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -7898768899143027088L;

    private static final Logger LOGGER = LoggerFactory.getLogger(MotifsServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /motifs doGet()");

        // Vérification si l'usager est connecté
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        try {
            LOGGER.info("Appel de la démarche afin de récupérer les motifs...");
            List<MotifDTO> motifs = getAfApiClient().getMotifs();
            ObjectMapper mapper = new ObjectMapper();
            String repJson = mapper.writeValueAsString(motifs);
            response.setContentType(MediaType.APPLICATION_JSON);
            IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
            response.setStatus(HttpStatus.SC_OK);
        } catch (Exception e) {
            LOGGER.error("MotifsServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            int codeStatut = getCodeErreur(e);
            response.setStatus(codeStatut);
        }
        LOGGER.info("====================== Fin /motifs doGet()");
    }
}

package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.servlet.dto.DocHolderFileSearchDTO;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DocHolderSearchServlet extends AbstractAfServlet {
    private final static long serialVersionUID = -314577095316396789L;
    private static Logger LOGGER = LoggerFactory.getLogger(DocHolderSearchServlet.class);
    private static final String serviceUrl = AfServletGouvPropertiesResolver.getPorteDocUrl() + "/search";

    /**
     * Méthode pour l'opération <b>searchFiles</b>
     * Elle permet de récupérer la liste de tous les documents enregistrés dans le porte-document de l'utilisateur
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("====================== " + req.getServletPath() + " doGet()");

        ObjectMapper mapper = new ObjectMapper();

        LOGGER.info("Vérification usager connecté");
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        DocHolderFileSearchDTO fileSearchDTO = new DocHolderFileSearchDTO();
        fileSearchDTO.setOperator(DocHolderFileSearchDTO.OperatorEnum.AND);

        Request serviceRequest = Request.Post(serviceUrl);
        serviceRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            serviceRequest.bodyString(mapper.writeValueAsString(fileSearchDTO), ContentType.APPLICATION_JSON);
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getStatusLine().getStatusCode();
            resp.setStatus(statusCode);

            if (statusCode == HttpStatus.SC_OK) {
                resp.setContentType(serviceResponse.getEntity().getContentType().getValue());
                IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());
            }
        } catch (IOException e) {
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents searchFiles", e);
        }

        LOGGER.info("====================== Fin " + req.getServletPath() + " doGet()");
    }
}

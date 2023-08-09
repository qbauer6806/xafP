package mc.gouv.xaf.servlet;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DocHolderCategoriesServlet extends AbstractAfServlet {
    private final static long serialVersionUID = -314577095316396789L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderCategoriesServlet.class);
    private static final String serviceUrl = AfServletGouvPropertiesResolver.getPorteDocUrl() + "/categories";

    /**
     * Méthode pour l'opération <b>getDocCategories</b>
     * Elle permet de récupérer toutes les catégories de documents disponibles pour le porte-documents
     *
     * @param resp L'objet de réponse, avec en valeur de retour un tableau de {@link String} ex : ["Photo", "Carte"]
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("====================== " + req.getServletPath() + " doGet()");

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        Request serviceRequest = Request.Get(serviceUrl);
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getStatusLine().getStatusCode();
            resp.setStatus(statusCode);
            resp.setContentType(serviceResponse.getEntity().getContentType().getValue());
            IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());
        }
        catch(ClientProtocolException e) {
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors de lexécution de l'appel à monguichet, erreur protocole HTTP", e);
        }
        catch (UnsupportedOperationException | IOException e) {
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents searchFiles", e);
        }

        LOGGER.info("====================== Fin " + req.getServletPath() + " doGet()");
    }
}

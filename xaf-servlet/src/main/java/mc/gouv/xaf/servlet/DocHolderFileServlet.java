package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.servlet.dto.DocHolderFileDTO;
import mc.gouv.xaf.servlet.dto.FileResponseDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DocHolderFileServlet extends AbstractAfServlet {
    private final static long serialVersionUID = -314577095316396789L;
    private static Logger LOGGER = LoggerFactory.getLogger(DocHolderFileServlet.class);

    /**
     * Méthode pour l'opération <b>getFile</b>
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String filename = req.getInputStream().toString();

        super.doGet(req, resp);
    }

    /**
     * Méthode pour l'opération <b>saveFile</b>
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String typedoc = req.getParameter("typedoc");
        String preferedName = req.getParameter("preferedName");

        if (StringUtils.isNotEmpty(typedoc) && StringUtils.isNotEmpty(preferedName)) {

            //JsonNode requestBody = req.getInputStream();

            FileResponseDTO responseDTO = new FileResponseDTO();

        } else {
            // ERREUR
        }

        super.doPost(req, resp);
    }

    /**
     * Méthode pour l'opération "deleteFile"
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("====================== " + req.getPathInfo() + " doDelete()");

        /*Request serviceRequest = Request.Delete(MOCKSERVER);
        serviceRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + AppFactoryServletUtils.getAuthHeader(AppFactoryServletUtils.ServiceTarget.FILE));

        try {
            serviceRequest.bodyStream(req.getInputStream());
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();

        } catch (IOException e) {
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents deleteFiles", e);
        }*/

        super.doDelete(req, resp);

        LOGGER.info("====================== Fin " + req.getPathInfo() + " doDelete()");
    }

    /**
     * Méthode pour l'opération <b>patchFile</b>
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("====================== " + req.getPathInfo() + " doPatch()");

        // String authorization = req.getHeader("Authorization"); ??????

        /*Request serviceRequest = Request.Patch(MOCKSERVER);
        serviceRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + AppFactoryServletUtils.getAuthHeader(AppFactoryServletUtils.ServiceTarget.FILE));

        try {
            serviceRequest.bodyStream(req.getInputStream());
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();

        } catch (IOException e) {
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents doPatch", e);
        }*/

        LOGGER.info("====================== Fin " + req.getPathInfo() + " doPatch()");
    }
}

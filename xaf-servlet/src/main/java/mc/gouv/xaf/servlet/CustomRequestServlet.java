package mc.gouv.xaf.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.servlet.dto.CustomRequestRechercheDTO;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.enums.HttpMethod;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DonneesExternesDTO;

/**
 * 
 * Servlet mettant à disposition le service /customRequest avec les méthodes PUT, POST, GET, DELETE. Cette servlet
 * permet d'appeler des fonctions API custom/spécifiques d'une démarche
 * 
 * @author qdeme
 *
 */
public class CustomRequestServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -7898768899143027088L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomRequestServlet.class);

    public void doHttpMethod(HttpServletRequest request, HttpServletResponse response, HttpMethod httpMethod) {

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        // Récupération de l'objet attaché à la session
        String sComplement = "";

        if (usagerInfosDTO.ismConnect()) {
            JsonNode usagerJson = usagerInfosDTO.getDonneesExternes();
            ObjectMapper omapper = new ObjectMapper();
            omapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            DonneesExternesDTO donneesMConnectDTO;
            try {
                donneesMConnectDTO = omapper.treeToValue(usagerJson, DonneesExternesDTO.class);
                sComplement = String.format("&FamilyName=%s&GivenName=%s&BirthDatetime=%s",
                        URLEncoder.encode(donneesMConnectDTO.getMconnect().getFamilyName(),
                                StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(donneesMConnectDTO.getMconnect().getGivenName(),
                                StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(
                                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
                                        .format(donneesMConnectDTO.getMconnect().getBirthDatetime()),
                                StandardCharsets.UTF_8.toString()));

            } catch (JsonProcessingException | UnsupportedEncodingException e) {
                LOGGER.error("CustomRequestServlet - Une erreur est survenue dans doHttpMethod: {}", e.getMessage(), e);
            }

        }

        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();
        LOGGER.info("UsagerID={}", usagerId);

        String pathInfo = request.getPathInfo();
        String restOfUrl = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            restOfUrl = "/" + pathInfo.split("/")[1];
        }

        String serviceUrl = AfServletGouvPropertiesResolver.getApiUrl() + "/customRequest";

        if (StringUtils.isNotBlank(restOfUrl)) {
            serviceUrl += restOfUrl;
        }

        serviceUrl += "?";

        if (StringUtils.isNotBlank(request.getQueryString())) {
            serviceUrl += request.getQueryString();
        }
        serviceUrl += sComplement;
        if (request.getParameter("usagerId") == null) {
            serviceUrl += "&usagerId=" + usagerInfosDTO.getId();
        }

        LOGGER.info("Appel à {}", serviceUrl);

        Request serviceRequest = null;
        String body = null;
        try {
            if (HttpMethod.GET.equals(httpMethod)) {
                serviceRequest = Request.Get(serviceUrl);
            } else if (HttpMethod.POST.equals(httpMethod)) {

                body = IOUtils.toString(request.getInputStream());

                ObjectMapper mapper = new ObjectMapper();
                try {
                    CustomRequestRechercheDTO rechercheInput = mapper.readValue(body, CustomRequestRechercheDTO.class);
                    if (rechercheInput.getData() != null) {
                        String sComplementPost = String.format("&numeroContrat=%s&numeroFacture=%s&numeroTiers=%s",
                                URLEncoder.encode(rechercheInput.getData().getNumeroContrat(),
                                        StandardCharsets.UTF_8.toString()),
                                URLEncoder.encode(rechercheInput.getData().getNumeroFacture(),
                                        StandardCharsets.UTF_8.toString()),
                                URLEncoder.encode(rechercheInput.getData().getNumeroTiers(),
                                        StandardCharsets.UTF_8.toString()));
                        serviceUrl += sComplementPost;
                    }
                } catch (Exception e) {
                    LOGGER.info("Exception lors de la deserialization de CustomRequestRechercheDTO{}", e);
                }
                serviceRequest = Request.Post(serviceUrl);
                serviceRequest.bodyByteArray(body.getBytes());
            } else if (HttpMethod.PUT.equals(httpMethod)) {
                serviceRequest = Request.Put(serviceUrl);
                body = IOUtils.toString(request.getInputStream());
                serviceRequest.bodyByteArray(body.getBytes());
            } else if (HttpMethod.DELETE.equals(httpMethod)) {
                serviceRequest = Request.Delete(serviceUrl);
            }
        } catch (IOException e) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "CustomRequestServlet - Une erreur est survenue lors de l'appel à la méthode " + httpMethod.name());
            return;
        }

        if (serviceRequest == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Situation anormale : serviceRequest == null");
            return;
        }
        serviceRequest.setHeader("Authorization", "Bearer " + AfServletGouvPropertiesResolver.getApiJwt());

        // Copier les headers
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String elem = headers.nextElement();
            if (!"Content-Length".equals(elem)) {
                serviceRequest.setHeader(elem, request.getHeader(elem));
            }
        }

        try {
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getStatusLine().getStatusCode();
            response.setStatus(statusCode);
            response.setContentType(serviceResponse.getEntity().getContentType().getValue());
            IOUtils.copy(serviceResponse.getEntity().getContent(), response.getOutputStream());
        } catch (Exception e) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Erreur lors du traitement de la réponse");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.info("====================== /customRequest doPost()");
        doHttpMethod(request, response, HttpMethod.POST);
        LOGGER.info("====================== Fin /customRequest doPost()");
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /customRequest doPut()");
        doHttpMethod(request, response, HttpMethod.PUT);
        LOGGER.info("====================== Fin /customRequest doPut()");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /customRequest doGet()");
        doHttpMethod(request, response, HttpMethod.GET);
        LOGGER.info("====================== Fin /customRequest doGet()");
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /demandes doDelete()");
        doHttpMethod(request, response, HttpMethod.DELETE);
        LOGGER.info("====================== Fin /demandes doDelete()");
    }
}

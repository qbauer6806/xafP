package mc.gouv.candifp.frontserver.movetoxaf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.candifp.frontserver.movetoxaf.dto.TgfApiIbanResponseDTO;
import mc.gouv.candifp.frontserver.movetoxaf.dto.UsagerInfosDTO;
import mc.gouv.candifp.frontserver.movetoxaf.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Servlet permettant l'appel à la méthode /verification-iban de l'API TGF
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/verification-iban")
public class VerificationIbanController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerificationIbanController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private FrontGouvPropertiesResolver propertiesResolver;

    @PostMapping
    public ResponseEntity doPost(HttpServletRequest request) {
        LOGGER.info("====================== /verification-iban doPost()");

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        Request serviceRequest = Request.Post(propertiesResolver.getTgfApiUrl());
        serviceRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + propertiesResolver.getTgfApiJwt());

        try {
            serviceRequest.bodyStream(request.getInputStream());
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getStatusLine().getStatusCode();
            ResponseEntity.BodyBuilder response = ResponseEntity.status(statusCode)
                    .contentType(MediaType.valueOf(serviceResponse.getEntity().getContentType().getValue()));

            String responseContent = IOUtils.toString(serviceResponse.getEntity().getContent());
            LOGGER.info("Response content: {}", responseContent);
            if (StringUtils.isBlank(responseContent)) {
                LOGGER.info("====================== Fin /verification-iban doPost()");
                return response.body(serviceResponse.getEntity().getContent());
            } else {
                ObjectMapper mapper = new ObjectMapper();
                TgfApiIbanResponseDTO responsePojo = mapper.readValue(responseContent, TgfApiIbanResponseDTO.class);

                if (responsePojo.getErreurs().length == 0) {
                    LOGGER.info("Final response OK");
                } else {
                    LOGGER.info("Final response NOK, with error list");
                    responsePojo.setErreurs(responsePojo.getErreurs());
                }
                LOGGER.info("====================== Fin /verification-iban doPost()");
                return response.body(responsePojo);
            }
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'appel à l'API TGF", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
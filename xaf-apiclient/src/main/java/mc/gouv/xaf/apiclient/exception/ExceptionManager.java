package mc.gouv.xaf.apiclient.exception;

import jakarta.ws.rs.core.Response;
import java.net.HttpURLConnection;
import mc.gouv.xapi.error.exception.WebException;
import mc.gouv.xapi.error.exception.client.BadRequestWebException;
import mc.gouv.xapi.error.exception.client.ForbiddenWebException;
import mc.gouv.xapi.error.exception.client.MethodNotAllowedWebException;
import mc.gouv.xapi.error.exception.client.NotAcceptableWebException;
import mc.gouv.xapi.error.exception.client.NotFoundWebException;
import mc.gouv.xapi.error.exception.client.UnauthorizedWebException;
import mc.gouv.xapi.error.exception.client.UnsupportedMediaTypeWebException;
import mc.gouv.xapi.error.exception.server.InternalErrorWebException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionManager.class);
    private ExceptionManager() {
    }

    public static void checkExceptionResponse(Response response) {

        // Si la réponse signale une erreur
        if (response.getStatus() < 200 || response.getStatus() > 299) {
            LOGGER.debug("Réponse reçue {}", response.readEntity(String.class));
            switch (response.getStatus()) {
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    throw response.readEntity(BadRequestWebException.class);
                case HttpURLConnection.HTTP_FORBIDDEN:
                    throw response.readEntity(ForbiddenWebException.class);
                case HttpURLConnection.HTTP_BAD_METHOD:
                    throw response.readEntity(MethodNotAllowedWebException.class);
                case HttpURLConnection.HTTP_NOT_ACCEPTABLE:
                    throw response.readEntity(NotAcceptableWebException.class);
                case HttpURLConnection.HTTP_NOT_FOUND:
                    throw response.readEntity(NotFoundWebException.class);
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    throw response.readEntity(UnauthorizedWebException.class);
                case HttpURLConnection.HTTP_UNSUPPORTED_TYPE:
                    throw response.readEntity(UnsupportedMediaTypeWebException.class);
                case HttpURLConnection.HTTP_INTERNAL_ERROR:
                    throw response.readEntity(InternalErrorWebException.class);
                default:
                    throw new WebException(response.getStatus(),
                            "Erreur lors de l'appel au service. Code HTTP " + response.getStatus());
            }

        }

    }

}

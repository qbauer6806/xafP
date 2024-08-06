package mc.gouv.xaf.api.error.jersey;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import mc.gouv.xapi.error.dto.ErrorsDTO;
import mc.gouv.xapi.error.exception.WebException;
import mc.gouv.xapi.error.exception.client.ForbiddenWebException;
import mc.gouv.xapi.error.exception.client.NotFoundWebException;
import mc.gouv.xapi.error.exception.client.UnsupportedMediaTypeWebException;
import mc.gouv.xapi.error.exception.server.InternalErrorWebException;

@Provider
public class ApiExceptionHandler implements ExceptionMapper<Exception> {

    private static final String ERROR_MESSAGE_404 = "url not found";

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @Override
    public Response toResponse(Exception ex) {
        LOGGER.error("Erreur : ", ex);
        if (ex instanceof WebException exception) {
            var errorsDTO = new ErrorsDTO();
            errorsDTO.setHttpStatus(exception.getHttpStatus());
            errorsDTO.setMessage(exception.getMessage());
            errorsDTO.setErrors(exception.getErrors());
            return Response.status(exception.getHttpStatus()).entity(errorsDTO).type(MediaType.APPLICATION_JSON)
                    .build();
        } else if (ex instanceof AccessDeniedException) {
            return toResponse(new ForbiddenWebException());
        } else if (ex instanceof NotFoundException) {
            return toResponse(new NotFoundWebException(ERROR_MESSAGE_404));
        } else if (ex instanceof NotSupportedException) {
            return toResponse(new UnsupportedMediaTypeWebException());
        } else {
            return toResponse(new InternalErrorWebException(ex));
        }
    }
}

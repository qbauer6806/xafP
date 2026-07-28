package mc.gouv.xaf.api.error.springmvc;

import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import mc.gouv.xapi.error.dto.ErrorsDTO;
import mc.gouv.xapi.error.exception.WebException;
import mc.gouv.xapi.error.exception.client.BadRequestWebException;
import mc.gouv.xapi.error.exception.client.ForbiddenWebException;
import mc.gouv.xapi.error.exception.client.MethodNotAllowedWebException;
import mc.gouv.xapi.error.exception.client.NotAcceptableWebException;
import mc.gouv.xapi.error.exception.client.NotFoundWebException;
import mc.gouv.xapi.error.exception.client.UnsupportedMediaTypeWebException;
import mc.gouv.xapi.error.exception.server.InternalErrorWebException;

@ControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String ERREUR_MSG = "Erreur lors de l'appel : {}";

    @ExceptionHandler(WebException.class)
    public @ResponseBody ErrorsDTO handleMetierWebException(HttpServletResponse res, WebException ex) {
        var errorsDTO = new ErrorsDTO();
        errorsDTO.setHttpStatus(ex.getHttpStatus());
        errorsDTO.setMessage(ex.getMessage());
        errorsDTO.setErrors(ex.getErrors());
        res.setStatus(ex.getHttpStatus());
        return errorsDTO;
    }

    /**
     * http://docs.spring.io/spring/docs/4.3.x/spring-framework-reference/html/mvc.html#mvc-exceptionhandlers-resolver
     */
    @ExceptionHandler({ HttpMessageNotReadableException.class, BindException.class,
            MissingServletRequestParameterException.class, TypeMismatchException.class,
            HttpMessageNotReadableException.class, MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class, MissingServletRequestPartException.class,
            TypeMismatchException.class })
    public @ResponseBody ErrorsDTO handle400Exception(HttpServletRequest req, HttpServletResponse res, Exception ex) {
        LOGGER.error(ERREUR_MSG, req.getPathInfo(), ex);
        return handleMetierWebException(res, new BadRequestWebException(ex));
    }

    @ExceptionHandler({ AccessDeniedException.class })
    public @ResponseBody ErrorsDTO handle403Exception(HttpServletRequest req, HttpServletResponse res, Exception ex) {
        LOGGER.error(ERREUR_MSG, req.getPathInfo(), ex);
        return handleMetierWebException(res, new ForbiddenWebException());
    }

    @ExceptionHandler({ NoHandlerFoundException.class })
    public @ResponseBody ErrorsDTO handle404Exception(HttpServletRequest req, HttpServletResponse res, Exception ex) {
        LOGGER.error(ERREUR_MSG, req.getPathInfo(), ex);
        return handleMetierWebException(res, new NotFoundWebException(ex));
    }

    @ExceptionHandler({ HttpRequestMethodNotSupportedException.class })
    public @ResponseBody ErrorsDTO handle405Exception(HttpServletRequest req, HttpServletResponse res, Exception ex) {
        LOGGER.error(ERREUR_MSG, req.getPathInfo(), ex);
        return handleMetierWebException(res, new MethodNotAllowedWebException(ex));
    }

    @ExceptionHandler({ HttpMediaTypeNotAcceptableException.class })
    public @ResponseBody ErrorsDTO handle406Exception(HttpServletRequest req, HttpServletResponse res, Exception ex) {
        LOGGER.error(ERREUR_MSG, req.getPathInfo(), ex);
        return handleMetierWebException(res, new NotAcceptableWebException(ex));
    }

    @ExceptionHandler({ HttpMediaTypeNotSupportedException.class })
    public @ResponseBody ErrorsDTO handle415Exception(HttpServletRequest req, HttpServletResponse res, Exception ex) {
        LOGGER.error(ERREUR_MSG, req.getPathInfo(), ex);
        return handleMetierWebException(res, new UnsupportedMediaTypeWebException(ex));
    }

    @ExceptionHandler({ ConstraintViolationException.class })
    public @ResponseBody ErrorsDTO handleConstraintException(HttpServletRequest req, HttpServletResponse res,
            ConstraintViolationException ex) {
        LOGGER.error(ERREUR_MSG, req.getPathInfo(), ex);
        Set<ConstraintViolation<?>> constraints = ex.getConstraintViolations();
        var badRequestWebException = new BadRequestWebException();

        if (constraints != null) {
            for (ConstraintViolation<?> c : constraints) {
                badRequestWebException.addError(c.getConstraintDescriptor().getAnnotation().toString(), c.getMessage(),
                        c.getPropertyPath().toString(), "0");
            }
        }

        return handleMetierWebException(res, badRequestWebException);
    }

    @ExceptionHandler({ Exception.class, RuntimeException.class })
    public @ResponseBody ErrorsDTO handleException(HttpServletRequest req, HttpServletResponse res, Exception ex) {
        LOGGER.error(ERREUR_MSG, req.getPathInfo(), ex);
        return handleMetierWebException(res, new InternalErrorWebException(ex));
    }

}

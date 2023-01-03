package mc.gouv.xaf.back.exception;

import org.springframework.http.HttpStatus;

/**
 * @author qdeme
 */
public class DemarchesServiceException extends RuntimeException {

    /**
     * Le code HTTP qu'il conviendrait de retourner pour ce message d'erreur
     */
    private final HttpStatus httpStatus;

    private static final long serialVersionUID = 1107512027061289756L;

    public DemarchesServiceException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String toString() {
        return "DemarchesServiceException [httpStatus=" + httpStatus + ", getMessage()=" + getMessage() + "]";
    }

}

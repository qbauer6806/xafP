package mc.gouv.xaf.shared.itg.resid.exception;

import java.io.IOException;
import java.util.List;

public class ResidHttpResponseException extends IOException {

    private int httpStatus;

    private String message;

    private List<Error> errors;

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }
}

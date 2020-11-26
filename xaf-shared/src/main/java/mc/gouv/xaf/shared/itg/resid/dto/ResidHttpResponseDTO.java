package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidHttpResponseDTO implements Serializable {

    private static final long serialVersionUID = 3891403896379854592L;

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

    @Override
    public String toString() {
        return "ResidHttpResponseDTO{" +
                "httpStatus=" + httpStatus +
                ", message='" + message + '\'' +
                ", errors=" + errors +
                '}';
    }
}

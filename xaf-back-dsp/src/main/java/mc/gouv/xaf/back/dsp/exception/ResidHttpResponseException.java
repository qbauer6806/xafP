package mc.gouv.xaf.back.dsp.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import mc.gouv.xaf.back.dsp.dto.ResidErrorDTO;
import mc.gouv.xaf.back.dsp.utils.MessageUtils;

import java.io.IOException;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidHttpResponseException extends IOException {

    private static final long serialVersionUID = -939170818731423898L;

    private int httpStatus;

    private String message;

    private List<ResidErrorDTO> errors;

    public ResidHttpResponseException() {
    }

    public ResidHttpResponseException(String message) {
        super(message);
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ResidErrorDTO> getErrors() {
        return errors;
    }

    public void setErrors(List<ResidErrorDTO> errors) {
        this.errors = errors;
    }

    public String toStringMessage() {
        return MessageUtils.toStringMessage(httpStatus, message, this.errors);
    }

    @Override
    public String toString() {
        return "ResidHttpResponseException{" +
                "httpStatus=" + httpStatus +
                ", message='" + message + '\'' +
                ", errors=" + errors +
                '}';
    }
}

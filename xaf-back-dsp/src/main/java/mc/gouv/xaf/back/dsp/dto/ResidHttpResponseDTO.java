package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import mc.gouv.xaf.back.dsp.utils.MessageUtils;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidHttpResponseDTO implements Serializable {

    private static final long serialVersionUID = 3891403896379854592L;

    private int httpStatus;

    private String message;

    private List<ResidErrorDTO> errors;
    
    private List<ResidWarningDTO> warnings;

    public List<ResidWarningDTO> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<ResidWarningDTO> warnings) {
		this.warnings = warnings;
	}

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
        return "ResidHttpResponseDTO{" +
                "httpStatus=" + httpStatus +
                ", message='" + message + '\'' +
                ", errors=" + errors +
                ", warnings=" + warnings +
                '}';
    }
}

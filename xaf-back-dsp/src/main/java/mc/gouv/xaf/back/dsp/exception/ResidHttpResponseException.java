package mc.gouv.xaf.back.dsp.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.IOException;
import java.io.Serial;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.dsp.dto.ResidErrorDTO;
import mc.gouv.xaf.back.dsp.utils.MessageUtils;

@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ResidHttpResponseException extends IOException {

    @Serial
    private static final long serialVersionUID = -939170818731423898L;

    @Getter
    private int httpStatus;

    private String message;

    @Getter
    private List<ResidErrorDTO> errors;

    public ResidHttpResponseException() {
    }

    public ResidHttpResponseException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String toStringMessage() {
        return MessageUtils.toStringMessage(httpStatus, message, this.errors);
    }

}

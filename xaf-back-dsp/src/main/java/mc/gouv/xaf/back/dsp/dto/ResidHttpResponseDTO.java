package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.dsp.utils.MessageUtils;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ResidHttpResponseDTO implements Serializable {

    private static final long serialVersionUID = 3891403896379854592L;

    private int httpStatus;

    private String message;

    private List<ResidErrorDTO> errors;
    
    private List<ResidWarningDTO> warnings;

    public String toStringMessage() {
        return MessageUtils.toStringMessage(httpStatus, message, this.errors);
    }

}

package mc.gouv.xaf.rio.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RioDocumentRequestDTO {

    private String codeApplication;
    private String lastModifier;
    private String codeNotice;

}

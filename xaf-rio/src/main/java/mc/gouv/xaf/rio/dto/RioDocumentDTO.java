package mc.gouv.xaf.rio.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RioDocumentDTO {

    private String codeApplication;
    private String lastModifier;
    private String codeNotice;
    private String refDocument;
    private Long keyDocument;
    private String creator;
    private String creationDate;
    private String modificationDate;
    private String eraser;
    private String removalDate;
    private List<RioFileDocumentDTO> attachments;

}

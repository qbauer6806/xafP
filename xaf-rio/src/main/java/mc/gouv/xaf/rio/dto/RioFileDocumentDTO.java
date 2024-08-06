package mc.gouv.xaf.rio.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class RioFileDocumentDTO {

    private Long keyFile;
    private String filename;
    private Integer rank;
    private Long fileSize;
    private String description;
}

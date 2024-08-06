package mc.gouv.xaf.shared.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

/**
 * Modélise un accès
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessDTO {

    private Integer pkAccess;

    private String demarcheId;

    private Integer usagerId;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateCreation;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateDerModif;

    private JsonNode contenu;
    
    @JsonIgnore
    private boolean updated = false;

}

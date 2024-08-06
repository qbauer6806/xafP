package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Modélise une donnée d'une demande
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemandeDataDTO {

    private Integer pkDemandesData;

    private Integer demandeId;
    
    private String key;

    private String value;
    
    @JsonIgnore
    boolean updated = false;

}

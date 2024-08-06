package mc.gouv.xaf.shared.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Modélise une stats de restitution des données 
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestitutionStatistiquesDTO {

    private Integer pkStatistique;

    private Integer usagerId;

    private Integer httpCode;
    
    private String message;

    private Date date;
    
    private String source;
    
    private String demarcheId;

}

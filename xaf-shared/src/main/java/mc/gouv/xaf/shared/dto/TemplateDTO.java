package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Modélise un template
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
public class TemplateDTO {

    private Integer pkTemplates;

    private String demarcheId;

    private String code;

    private String contenu;

    private String langue;

    private Date dateModif;
    
    @JsonIgnore
    private boolean updated = false;

}

package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * Modélise un template
 *
 * @author qdeme
 */
@Setter
@Getter
public class SmsTemplateDTO {

    private Integer pkSmsTemplates;

    private String code;
    
    private String sender;

    private String contenu;

    private String langue;

    private Date dateModif;

    @JsonIgnore
    private boolean updated = false;

}

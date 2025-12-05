package mc.gouv.xaf.shared.dto;

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
public class ExportTemplateDTO {

    private String code;

    private String contenu;

    private String langue;

    private Date dateModif;

}

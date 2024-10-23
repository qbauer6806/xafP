package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Représente le contenu d'une ligne d'historique de demande
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemandeHistoriqueContenuDTO {

    private String statutName;
    
    private String html;

    public DemandeHistoriqueContenuDTO(String name, String role, String action, String statutName) {
        StringBuilder htmlBuilder = new StringBuilder("<span class='histo-user'>").append(role);
        if (StringUtils.isNotBlank(name)) {
            htmlBuilder.append(' ').append(name);
        }
        htmlBuilder.append("</span><span class='histo-separator'></span><span>").append(action).append("</span>");
        this.html = htmlBuilder.toString();
        this.statutName = statutName;
    }

}

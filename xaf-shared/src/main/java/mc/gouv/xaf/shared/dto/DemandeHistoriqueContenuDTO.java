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

    private StatutPublicOuInterneDTO statutPublicOuInterne;
    
    private String html;
    
    private String texte;
    
    private String usagerNom;
    
    private String utilisateurNom;

    public DemandeHistoriqueContenuDTO(String name, String role, String action, StatutPublicOuInterneDTO spoi) {
        StringBuilder htmlBuilder = new StringBuilder("<span class='histo-user'>").append(role);
        StringBuilder texteBuilder = new StringBuilder(role);
        if (StringUtils.isNotBlank(name)) {
            htmlBuilder.append(' ').append(name);
            texteBuilder.append(' ').append(name);
        }
        htmlBuilder.append("</span><span class='histo-separator'></span><span>").append(action).append("</span>");
        texteBuilder.append(" : ").append(action);
        this.html = htmlBuilder.toString();
        this.texte = texteBuilder.toString();
        this.statutPublicOuInterne = spoi;
    }

}

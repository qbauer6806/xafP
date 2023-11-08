package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;

/**
 * Représente le contenu d'une ligne d'historique de demande
 * 
 * @author qdeme
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemandeHistoriqueContenuDTO {

    private StatutPublicOuInterneDTO statutPublicOuInterne;
    
    private String html;
    
    private String texte;
    
    private String usagerNom;
    
    private String utilisateurNom;

    public DemandeHistoriqueContenuDTO() {
    }

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

    public StatutPublicOuInterneDTO getStatutPublicOuInterne() {
        return statutPublicOuInterne;
    }

    public void setStatutPublicOuInterne(StatutPublicOuInterneDTO statutPublicOuInterne) {
        this.statutPublicOuInterne = statutPublicOuInterne;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getUsagerNom() {
        return usagerNom;
    }

    public void setUsagerNom(String usagerNom) {
        this.usagerNom = usagerNom;
    }

    public String getUtilisateurNom() {
        return utilisateurNom;
    }

    public void setUtilisateurNom(String utilisateurNom) {
        this.utilisateurNom = utilisateurNom;
    }
    
}

#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.dto;

import mc.gouv.af.back.dto.StatutPublicOuInterneDTO;

/**
 * Représente le contenu d'une ligne d'historique de demande selon ${artifactIdCamelCase}
 * 
 * @author mpavone
 *
 */
public class ${artifactIdCamelCase}DemandeHistoriqueContenuDTO {

    private StatutPublicOuInterneDTO statutPublicOuInterne;
    
    private String html;
    
    private String texte;
    
    private String usagerNom;
    
    private String utilisateurNom;

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

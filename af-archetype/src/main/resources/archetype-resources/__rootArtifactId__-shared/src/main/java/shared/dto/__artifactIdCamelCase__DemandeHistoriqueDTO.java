#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.dto;

import mc.gouv.dem.shared.model.DemandeHistoriqueDTO;

/**
 * Représente une ligne d'historique de demande selon ${artifactIdCamelCase}
 * 
 * @author qdeme
 *
 */
public class ${artifactIdCamelCase}DemandeHistoriqueDTO {
    
    private DemandeHistoriqueDTO demHistorique;
    
    private ${artifactIdCamelCase}DemandeHistoriqueContenuDTO contenu;

    public DemandeHistoriqueDTO getDemHistorique() {
        return demHistorique;
    }

    public void setDemHistorique(DemandeHistoriqueDTO demHistorique) {
        this.demHistorique = demHistorique;
    }

    public ${artifactIdCamelCase}DemandeHistoriqueContenuDTO getContenu() {
        return contenu;
    }

    public void setContenu(${artifactIdCamelCase}DemandeHistoriqueContenuDTO contenu) {
        this.contenu = contenu;
    }
    
}

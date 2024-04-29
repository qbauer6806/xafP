package mc.gouv.xaf.shared.dto;

/**
 * Représente une ligne d'historique de demande pour affichage sur une page
 * Le DemandeHistoriqueDTO qui représente ce qu'il est en base, + le contenu POJOifié
 * afin de pouvoir référencer les éléments du contenu depuis la page avec Thymeleaf
 * 
 * @author qdeme
 *
 */
public class DemandeHistoriqueAffichageDTO {
    
    private DemandeHistoriqueDTO demHistorique;
    
    private DemandeHistoriqueContenuDTO contenu;

    public DemandeHistoriqueDTO getDemHistorique() {
        return demHistorique;
    }

    public void setDemHistorique(DemandeHistoriqueDTO demHistorique) {
        this.demHistorique = demHistorique;
    }

    public DemandeHistoriqueContenuDTO getContenu() {
        return contenu;
    }

    public void setContenu(DemandeHistoriqueContenuDTO contenu) {
        this.contenu = contenu;
    }
    
}

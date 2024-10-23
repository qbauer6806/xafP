package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Représente une ligne d'historique de demande pour affichage sur une page Le DemandeHistoriqueDTO qui représente ce
 * qu'il est en base, + le contenu POJOifié afin de pouvoir référencer les éléments du contenu depuis la page avec
 * Thymeleaf
 *
 * @author qdeme
 */
@Setter
@Getter
public class DemandeHistoriqueAffichageDTO {

    private DemandeHistoriqueDTO demHistorique;

    private DemandeHistoriqueContenuDTO contenu;

}

package mc.gouv.xaf.back.paiement.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CommandeDemandeArticleDTO {
    private Integer pkCommandesDemandesArticles;

    private Integer fkCommandeDemande;

    private String codeTarif;

    private double montant;

}

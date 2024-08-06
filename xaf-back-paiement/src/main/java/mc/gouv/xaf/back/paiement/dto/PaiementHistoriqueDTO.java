package mc.gouv.xaf.back.paiement.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.paiement.enums.PaiementStatutEnum;

import java.time.LocalDateTime;

/**
 * Modélise le contenu d'une ligne d'historique
 *
 * @author qdeme
 */
@Setter
@Getter
@ToString
public class PaiementHistoriqueDTO {

    private Integer pkHistorique;

    private Integer fkDemandes;

    private LocalDateTime date;

    private PaiementStatutEnum statut;

    private String couleur;

    private Integer usagerId;

    private String contenu;

}

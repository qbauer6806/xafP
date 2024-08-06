package mc.gouv.xaf.back.paiement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandeDemandeDTO {

    private Integer pkCommandeDemandes;

    private Integer fkCommandes;

    private Integer fkDemandes;

    private Double montant;

    private List<CommandeDemandeArticleDTO> commandeDemandeArticles;

}

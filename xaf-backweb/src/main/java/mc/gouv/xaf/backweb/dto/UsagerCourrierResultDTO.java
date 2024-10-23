package mc.gouv.xaf.backweb.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Contient les données à afficher dans la page de recherche d'usagers courrier
 *
 * @author qdeme
 */
@Setter
@Getter
public class UsagerCourrierResultDTO {

    private Integer usagerId;

    private String nomRaisonSociale;

    private String nomPrenom;

    private String codePostal;

    private String ville;

    private String adresse;

    private String raisonSociale;

    private int nbDemandes;

}

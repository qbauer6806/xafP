package mc.gouv.xaf.backweb.denjs.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO pour affichage des données du tableau de la page de gestion des agents
 *
 * @author qdeme
 */
@Setter
@Getter
public class DenjsAgentEtablissementDTO {

    private String agentNom;

    private String agentMatricule;

    private String etablissementCode;

    private String etablissementNom;

}

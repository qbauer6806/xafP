package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;

/**
 * Input de WS pour les demandes
 *
 * @author qdeme
 */
@Setter
@Getter
@ToString
public class DemandeInputDTO {

    private JsonNode contenu;

    private JsonNode contenuInitial;

    private DemandeFileDTO[] fichiers;

    private String langue;

    private DemandeCanalEnum canal;

    private String observations;

    private String agentAffecteId;

    private Date courrierDateReception;

    private String courrierRefInterne;

    private String creeParAgentId;

    private boolean novalidate;

    private String recapType;

    private Integer brouillonId;

    private JsonNode meta;

    // Données envoyées à l'API si l'usager s'est connecté via MConnect
    private DonneesMConnectDTO donneesMConnect;

    // En cas de renouvellement d'une demande
    private Integer demandeSourceId;

    private JsonNode donneesExternes;
    
    // Champ pour solution 2/3 : fk vers la config stockée dans l'API GenTS (build_id). A stocker par système tiers.
    private String buildId;

}

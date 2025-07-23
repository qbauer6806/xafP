package mc.gouv.xaf.shared.dto;

import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;

/**
 * DTO interne (non partagé) servant à DEM pour regrouper des critères de recherche de demandes
 *
 * @author qdeme
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class DemandeRechercheDTO {

    private String texte;

    private List<String> statuts;

    private List<DemandeCanalEnum> canaux;

    private String agentAffecteId;

    private Integer usagerId;

    private Date creationStartDate;

    private Date creationEndDate;

    // Pour le moment on gère la recherche pour une data
    private DataRechercheDTO data;

    private String identifiant;

    private String[] searchFields;

    private boolean aucunCanal;

    private boolean aucunStatut;

    private boolean aucunAgentAffecte;

    private boolean checkTimestamp;

    private boolean trad = true;

    public DemandeRechercheDTO(String texte, List<String> statuts, List<DemandeCanalEnum> canaux, String agentAffecteId,
            Integer usagerId, Date creationStartDate, Date creationEndDate, DataRechercheDTO data, String identifiant) {
        super();
        this.texte = texte;
        this.statuts = statuts;
        this.canaux = canaux;
        this.agentAffecteId = agentAffecteId;
        this.usagerId = usagerId;
        this.creationStartDate = creationStartDate;
        this.creationEndDate = creationEndDate;
        this.data = data;
        this.identifiant = identifiant;
    }

}

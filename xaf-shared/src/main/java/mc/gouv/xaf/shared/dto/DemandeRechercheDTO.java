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
 *
 */
@Setter
@ToString
@NoArgsConstructor
public class DemandeRechercheDTO {

    @Getter
    private String texte;

    @Getter
    private List<String> statuts;

    @Getter
    private List<DemandeCanalEnum> canaux;

    @Getter
    private String agentAffecteId;

    @Getter
    private Integer usagerId;

    @Getter
    private Date creationStartDate;

    @Getter
    private Date creationEndDate;

    // Pour le moment on gère la recherhe pour une data
    @Getter
    private DataRechercheDTO data;

    @Getter
    private String identifiant;

    @Getter
    private String[] searchFields;

    private boolean aucunCanal;

    private boolean aucunStatut;

    @Getter
    private boolean aucunResponsable;

    @Getter
    private boolean checkTimestamp;

    public DemandeRechercheDTO(String texte, List<String> statuts, List<DemandeCanalEnum> canaux,
                               String agentAffecteId, Integer usagerId, Date creationStartDate, Date creationEndDate,
                               DataRechercheDTO data, String identifiant) {
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

    public boolean getAucunCanal() {
        return aucunCanal;
    }

    public boolean getAucunStatut() {
        return aucunStatut;
    }

}

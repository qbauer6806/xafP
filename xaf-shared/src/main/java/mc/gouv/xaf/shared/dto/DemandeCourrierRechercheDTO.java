package mc.gouv.xaf.shared.dto;

import java.util.Date;
import java.util.List;

import lombok.Setter;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;

/**
 * DTO interne regrouper des critères de recherche des courriers
 *
 * @author mpavone
 */
@Setter
public class DemandeCourrierRechercheDTO extends DemandeRechercheDTO {

    private boolean imprime;

    public DemandeCourrierRechercheDTO() {
        super();
    }

    public DemandeCourrierRechercheDTO(boolean imprime) {
        this.imprime = imprime;
    }

    public DemandeCourrierRechercheDTO(String texte, List<String> statuts, List<DemandeCanalEnum> canaux,
            String agentAffecteId, Integer usagerId, Date creationStartDate, Date creationEndDate,
            DataRechercheDTO data, String identifiant, boolean imprime) {
        super(texte, statuts, canaux, agentAffecteId, usagerId, creationStartDate, creationEndDate, data, identifiant);
        this.imprime = imprime;
    }

    public boolean getImprime() {
        return imprime;
    }

}

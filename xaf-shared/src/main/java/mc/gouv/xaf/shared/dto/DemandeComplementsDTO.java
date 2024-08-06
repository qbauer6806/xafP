package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.shared.enums.DemandeComplementsStatutEnum;

/**
 * Modélise une demande d'informations complémentaires
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
public class DemandeComplementsDTO {

    private Integer pkDemandeComplements;
    
    private Integer demandeId;
    
    private DemandeComplementsStatutEnum statut;
    
    @JsonIgnore
    private boolean updated = false;
    
    private DemandeComplementsQuestionDTO question;
    
    private DemandeComplementsReponseDTO reponse;

}

package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import mc.gouv.xaf.shared.enums.DemandeComplementsStatutEnum;

/**
 * Modélise une demande d'informations complémentaires
 * 
 * @author qdeme
 *
 */
public class DemandeComplementsDTO {

    private Integer pkDemandeComplements;
    
    private Integer demandeId;
    
    private DemandeComplementsStatutEnum statut;
    
    @JsonIgnore
    private boolean updated = false;
    
    private DemandeComplementsQuestionDTO question;
    
    private DemandeComplementsReponseDTO reponse;
    
    public Integer getPkDemandeComplements() {
        return pkDemandeComplements;
    }

    public void setPkDemandeComplements(Integer pkDemandeComplements) {
        this.pkDemandeComplements = pkDemandeComplements;
    }

    public Integer getDemandeId() {
        return demandeId;
    }

    public void setDemandeId(Integer demandeId) {
        this.demandeId = demandeId;
    }

    public DemandeComplementsStatutEnum getStatut() {
        return statut;
    }

    public void setStatut(DemandeComplementsStatutEnum statut) {
        this.statut = statut;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public DemandeComplementsQuestionDTO getQuestion() {
        return question;
    }

    public void setQuestion(DemandeComplementsQuestionDTO question) {
        this.question = question;
    }

    public DemandeComplementsReponseDTO getReponse() {
        return reponse;
    }

    public void setReponse(DemandeComplementsReponseDTO reponse) {
        this.reponse = reponse;
    }
    
}

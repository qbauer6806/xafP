package mc.gouv.xaf.shared.dto;

import java.util.Date;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Modélise le contenu d'une ligne d'historique
 * 
 * @author qdeme
 *
 */
public class DemandeHistoriqueDTO {

    private Integer pkDemandeHistorique;

    private Integer fkDemandes;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date date;

    private DemandeStatutDTO fkStatut;

    private String agentId;

    private Integer usagerId;

    @NotNull
    private JsonNode contenu;

    public Integer getPkDemandeHistorique() {
        return pkDemandeHistorique;
    }

    public void setPkDemandeHistorique(Integer pkDemandeHistorique) {
        this.pkDemandeHistorique = pkDemandeHistorique;
    }

    public Integer getFkDemandes() {
        return fkDemandes;
    }

    public void setFkDemandes(Integer fkDemandes) {
        this.fkDemandes = fkDemandes;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public DemandeStatutDTO getFkStatut() {
        return fkStatut;
    }

    public void setFkStatut(DemandeStatutDTO fkStatut) {
        this.fkStatut = fkStatut;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Integer getUsagerId() {
        return usagerId;
    }

    public void setUsagerId(Integer usagerId) {
        this.usagerId = usagerId;
    }

    public JsonNode getContenu() {
        return contenu;
    }

    public void setContenu(JsonNode contenu) {
        this.contenu = contenu;
    }

    @Override
    public String toString() {
        return "DemandeHistoriqueDTO [pkDemandeHistorique=" + pkDemandeHistorique + ", fkDemandes=" + fkDemandes
                + ", date=" + date + ", fkStatut=" + fkStatut + ", agentId=" + agentId + ", usagerId=" + usagerId
                + ", contenu=" + contenu + "]";
    }

}

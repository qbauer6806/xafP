package mc.gouv.xaf.back.shared.dto;

import java.util.Date;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Modélise la partie "Réponse" d'une demande d'informations complémentaires
 * 
 * @author qdeme
 *
 */
public class DemandeComplementsReponseDTO {

    @NotNull
    private String texte;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date date;

    private Integer usagerId;

    private String agentId;

    @JsonInclude(Include.NON_NULL)
    private DemandeComplementsFileDTO[] fichiers;

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getUsagerId() {
        return usagerId;
    }

    public void setUsagerId(Integer usagerId) {
        this.usagerId = usagerId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public DemandeComplementsFileDTO[] getFichiers() {
        return fichiers;
    }

    public void setFichiers(DemandeComplementsFileDTO[] fichiers) {
        this.fichiers = fichiers;
    }

}

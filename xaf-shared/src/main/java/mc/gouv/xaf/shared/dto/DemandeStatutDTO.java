package mc.gouv.xaf.shared.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Modélise un statut d'une demande
 * 
 * @author qdeme
 *
 */
public class DemandeStatutDTO {

    public static final String LIBELLE_FIELD_NAME = "libelle";

    private Integer pkStatut;

    private String libelle;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date date;

    private String agentId;

    private Integer usagerId;

    private String codeMotif;

    private String commentaire;

    private String texteAEnvoyer;

    public Integer getPkStatut() {
        return pkStatut;
    }

    public void setPkStatut(Integer pkStatut) {
        this.pkStatut = pkStatut;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public String getCodeMotif() {
        return codeMotif;
    }

    public void setCodeMotif(String codeMotif) {
        this.codeMotif = codeMotif;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getTexteAEnvoyer() {
        return texteAEnvoyer;
    }

    public void setTexteAEnvoyer(String texteAEnvoyer) {
        this.texteAEnvoyer = texteAEnvoyer;
    }

    @Override
    public String toString() {
        return "DemandeStatutDTO [pkStatut=" + pkStatut + ", libelle=" + libelle + ", date=" + date + ", agentId="
                + agentId + ", usagerId=" + usagerId + ", codeMotif=" + codeMotif + ", commentaire=" + commentaire
                + ", texteAEnvoyer=" + texteAEnvoyer + "]";
    }

}

package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.shared.enums.StatutTachesEnum;

/**
 * @author mboutelier.ext
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TacheDTO {

    private Integer pkTaches;

    private Integer fkDemande;

    private StatutTachesEnum statutAgent;

    private StatutTachesEnum statutValideur;

    private String codeMotif;

    private String codeType;

    private String commentaire;

    private JsonNode contenu;

    private boolean locked;

    public Integer getPkTaches() {
        return pkTaches;
    }

    public void setPkTaches(Integer pkTaches) {
        this.pkTaches = pkTaches;
    }

    public Integer getFkDemande() {
        return fkDemande;
    }

    public void setFkDemande(Integer fkDemande) {
        this.fkDemande = fkDemande;
    }

    public StatutTachesEnum getStatutAgent() {
        return statutAgent;
    }

    public void setStatutAgent(String codeStatutAgent) {
        if (null != codeStatutAgent) {
            this.statutAgent = StatutTachesEnum.valueOf(codeStatutAgent);
        }
    }

    public StatutTachesEnum getStatutValideur() {
        return statutValideur;
    }

    public void setStatutValideur(String codeStatutValideur) {
        this.statutValideur = null != codeStatutValideur ? StatutTachesEnum.valueOf(codeStatutValideur) : null;
    }

    public String getCodeMotif() {
        return codeMotif;
    }

    public void setCodeMotif(String codeMotif) {
        this.codeMotif = codeMotif;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public JsonNode getContenu() {
        return contenu;
    }

    public void setContenu(JsonNode contenu) {
        this.contenu = contenu;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}

package mc.gouv.xaf.shared.dto;

import java.util.Date;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Représente un motif
 * 
 * @author qdeme
 *
 */
public class MotifDTO {

    public static final String LANG_FR = "fr";
    public static final String LANG_EN = "en";

    private Integer pkMotifs;

    private String demarcheId;

    @NotNull
    private String code;

    @NotNull
    private String libelle;

    @NotNull
    private String statut;

    @NotNull
    private String langue;

    @JsonIgnore
    private boolean updated = false;

    private Date dateArchive;
    
    private String commentairePrerempli;

    private String texteAEnvoyer;

    public Integer getPkMotifs() {
        return pkMotifs;
    }

    public void setPkMotifs(Integer pkMotifs) {
        this.pkMotifs = pkMotifs;
    }

    public String getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(String demarcheId) {
        this.demarcheId = demarcheId;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getDateArchive() {
        return dateArchive;
    }

    public void setDateArchive(Date dateArchive) {
        this.dateArchive = dateArchive;
    }
    
    public String getCommentairePrerempli() {
        return commentairePrerempli;
    }

    public void setCommentairePrerempli(String commentairePrerempli) {
        this.commentairePrerempli = commentairePrerempli;
    }

    public String getTexteAEnvoyer() {
        return texteAEnvoyer;
    }

    public void setTexteAEnvoyer(String texteAEnvoyer) {
        this.texteAEnvoyer = texteAEnvoyer;
    }

    @Override
    public Object clone() {
        MotifDTO motifDTO = new MotifDTO();
        motifDTO.pkMotifs = this.pkMotifs;
        motifDTO.demarcheId = this.demarcheId;
        motifDTO.code = this.code;
        motifDTO.libelle = this.libelle;
        motifDTO.statut = this.statut;
        motifDTO.langue = this.langue;
        motifDTO.updated = this.updated;
        motifDTO.dateArchive = this.dateArchive;
        motifDTO.commentairePrerempli = this.commentairePrerempli;
        motifDTO.texteAEnvoyer = this.texteAEnvoyer;
        return motifDTO;
    }
}

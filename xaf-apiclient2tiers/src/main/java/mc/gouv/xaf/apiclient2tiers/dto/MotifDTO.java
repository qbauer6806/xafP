package mc.gouv.xaf.apiclient2tiers.dto;

import java.util.Date;

import jakarta.validation.constraints.NotNull;

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

    public MotifDTO() {
        super();
    }

    /**
     * Constructeur remplaçant la méthode clone()<br>
     * Copie l'objet source donné en paramètre.
     *
     * @param source l'objet à copier
     */
    public MotifDTO(MotifDTO source) {
        super();
        this.pkMotifs = source.getPkMotifs();
        this.demarcheId = source.getDemarcheId();
        this.code = source.getCode();
        this.libelle = source.getLibelle();
        this.statut = source.getStatut();
        this.langue = source.getLangue();
        this.updated = source.isUpdated();
        this.dateArchive = source.getDateArchive();
        this.commentairePrerempli = source.getCommentairePrerempli();
        this.texteAEnvoyer = source.getTexteAEnvoyer();
    }

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

}

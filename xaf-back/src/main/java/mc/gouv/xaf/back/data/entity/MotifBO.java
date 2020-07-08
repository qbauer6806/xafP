package mc.gouv.xaf.back.data.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * Classe BO de la table DEM.MOTIFS
 * 
 * @author qdeme
 *
 */
@Entity
@Table(name = "DEM_MOTIFS")
public class MotifBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_MOTIFS", nullable = false)
    private Integer pkMotifs;

    @Column(name = "FK_DEMARCHEID", length = 128, nullable = false)
    @NotBlank
    @Size(min = 1, max = 128)
    private String demarcheId;

    @Column(name = "CODE", length = 128, nullable = false)
    @NotBlank
    @Size(min = 1, max = 128)
    private String code;

    @Column(name = "LIBELLE", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String libelle;

    @Column(name = "STATUT", length = 64, nullable = false)
    @NotBlank
    @Size(min = 1, max = 64)
    private String statut;

    @Column(name = "LANGUE", length = 2, nullable = false)
    @NotBlank
    @Size(min = 1, max = 2)
    private String langue;

    @Column(name = "DATE_ARCHIVE", nullable = true)
    private Date dateArchive;
    
    @Column(name = "COMMENTAIRE_PREREMPLI", length = 2048, nullable = true)
    @Size(min = 0, max = 2048)
    private String commentairePrerempli;

    @Column(name = "TEXTE_A_ENVOYER", length = 2048, nullable = true)
    @Size(min = 0, max = 2048)
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

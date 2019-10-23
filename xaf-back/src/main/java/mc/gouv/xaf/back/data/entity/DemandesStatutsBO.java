package mc.gouv.xaf.back.data.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * Classe BO de la table DEM.STATUTS
 * 
 * @author qdeme
 *
 */
@Entity
@Table(name = "DEM_DEMANDES_STATUTS")
public class DemandesStatutsBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDESSTATUTS", nullable = false)
    private Integer pkDemandesStatuts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_DEMANDES")
    private DemandeBO fkDemandes;

    @Column(name = "LIBELLE", length = 64, nullable = false)
    @NotBlank
    @Size(min = 1, max = 64)
    private String libelle;

    @Column(name = "DATE", nullable = false)
    private Date date;

    @Column(name = "AGENT_ID", length = 128, nullable = true)
    @Size(min = 0, max = 128)
    private String agentId;

    @Column(name = "USAGER_ID", nullable = true)
    private Integer usagerId;

    @Column(name = "CODE_MOTIF", length = 128, nullable = true)
    @Size(min = 0, max = 128)
    private String codeMotif;

    @Column(name = "COMMENTAIRE", length = 8000, nullable = true)
    @Size(min = 0, max = 8000)
    private String commentaire;

    @Column(name = "TEXTE_A_ENVOYER", length = 8000, nullable = true)
    @Size(min = 0, max = 8000)
    private String texteAEnvoyer;

    public Integer getPkDemandesStatuts() {
        return pkDemandesStatuts;
    }

    public void setPkDemandesStatuts(Integer pkDemandesStatuts) {
        this.pkDemandesStatuts = pkDemandesStatuts;
    }

    public DemandeBO getFkDemandes() {
        return fkDemandes;
    }

    public void setFkDemandes(DemandeBO fkDemandes) {
        this.fkDemandes = fkDemandes;
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
        return "DemandesStatutsBO [libelle=" + libelle + ", date=" + date + ", agentId=" + agentId + ", usagerId="
                + usagerId + ", codeMotif=" + codeMotif + ", commentaire=" + commentaire+ ", textAEnvoyer=" + texteAEnvoyer + "]";
    }
    
    

}

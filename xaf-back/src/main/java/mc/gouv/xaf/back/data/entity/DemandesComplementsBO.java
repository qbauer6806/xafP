package mc.gouv.xaf.back.data.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * Classe BO de la table DEM.DEMANDES_COMPLEMENTS
 * 
 * @author qdeme
 *
 */
@Entity
@Table(name = "DEM_DEMANDES_COMPLEMENTS")
public class DemandesComplementsBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDESCOMPLEMENTS", nullable = false)
    private Integer pkDemandesComplements;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDES")
    private DemandeBO fkDemandes;

    @Column(name = "DATE_CREATION", nullable = false)
    private Date dateCreation;

    @Column(name = "DATE_REPONSE", nullable = true)
    private Date dateReponse;

    @Column(name = "CODE_MOTIF", length = 128, nullable = false)
    @NotBlank
    @Size(min = 1, max = 128)
    private String codeMotif;

    @Column(name = "QUESTION", length = 8000, nullable = true)
    @Size(min = 0, max = 8000)
    private String question;

    @Column(name = "REPONSE", length = 8000, nullable = true)
    @Size(min = 0, max = 8000)
    private String reponse;

    @Column(name = "STATUT", length = 64, nullable = false)
    @NotBlank
    @Size(min = 1, max = 64)
    private String statut;

    @Column(name = "AGENT_ID", length = 128, nullable = false)
    @NotBlank
    @Size(min = 1, max = 128)
    private String agentId;

    @Column(name = "REPONSE_AGENT_ID", length = 128, nullable = true)
    @Size(min = 0, max = 128)
    private String reponseAgentId;

    @Column(name = "REPONSE_USAGER_ID", nullable = true)
    private Integer reponseUsagerId;

    @OneToMany(mappedBy = "fkDemandesComplements", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DemandesComplementsFilesBO> files;

    public Integer getPkDemandesComplements() {
        return pkDemandesComplements;
    }

    public void setPkDemandesComplements(Integer pkDemandesComplements) {
        this.pkDemandesComplements = pkDemandesComplements;
    }

    public DemandeBO getFkDemandes() {
        return fkDemandes;
    }

    public void setFkDemandes(DemandeBO fkDemandes) {
        this.fkDemandes = fkDemandes;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Date getDateReponse() {
        return dateReponse;
    }

    public void setDateReponse(Date dateReponse) {
        this.dateReponse = dateReponse;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getReponseAgentId() {
        return reponseAgentId;
    }

    public void setReponseAgentId(String reponseAgentId) {
        this.reponseAgentId = reponseAgentId;
    }

    public Integer getReponseUsagerId() {
        return reponseUsagerId;
    }

    public void setReponseUsagerId(Integer reponseUsagerId) {
        this.reponseUsagerId = reponseUsagerId;
    }

    public Set<DemandesComplementsFilesBO> getFiles() {
        return files;
    }

    public void setFiles(Set<DemandesComplementsFilesBO> files) {
        this.files = files;
    }

    public String getCodeMotif() {
        return codeMotif;
    }

    public void setCodeMotif(String codeMotif) {
        this.codeMotif = codeMotif;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

}

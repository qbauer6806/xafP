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
 * Classe BO de la table DEM.HISTORIQUE
 * 
 * @author qdeme
 *
 */
@Entity
@Table(name = "DEM_DEMANDES_HISTORIQUE")
public class DemandesHistoriqueBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDESHISTORIQUE", nullable = false)
    private Integer pkDemandesHistorique;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDES", nullable = false)
    private DemandeBO fkDemandes;

    @Column(name = "DATE", nullable = false)
    private Date date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_STATUT", nullable = false)
    private DemandesStatutsBO fkStatut;

    @Column(name = "AGENT_ID", length = 128, nullable = true)
    @Size(min = 0, max = 128)
    private String agentId;

    @Column(name = "USAGER_ID", nullable = true)
    private Integer usagerId;

    @Column(name = "JUSTIFICATIF_TRAITEMENT", length = 8000)
    private String justificatifTraitement;

    @Column(name = "CONTENU", length = 10000, nullable = false)
    @NotBlank
    @Size(min = 1, max = 10000)
    private String contenu;

    public Integer getPkDemandesHistorique() {
        return pkDemandesHistorique;
    }

    public void setPkDemandesHistorique(Integer pkDemandesHistorique) {
        this.pkDemandesHistorique = pkDemandesHistorique;
    }

    public DemandeBO getFkDemandes() {
        return fkDemandes;
    }

    public void setFkDemandes(DemandeBO fkDemandes) {
        this.fkDemandes = fkDemandes;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public DemandesStatutsBO getFkStatut() {
        return fkStatut;
    }

    public void setFkStatut(DemandesStatutsBO fkStatut) {
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

    public String getJustificatifTraitement() {
        return justificatifTraitement;
    }

    public void setJustificatifTraitement(String justificatifTraitement) {
        this.justificatifTraitement = justificatifTraitement;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
}

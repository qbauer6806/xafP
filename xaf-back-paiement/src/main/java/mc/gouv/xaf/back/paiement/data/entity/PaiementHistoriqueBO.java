package mc.gouv.xaf.back.paiement.data.entity;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

/**
 * Classe BO de la table PMNT_HISTORIQUE
 *
 * @author mboutelier.ext
 */
@Entity
@Table(name = "PMNT_HISTORIQUE")
public class PaiementHistoriqueBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_HISTORIQUE", nullable = false)
    private Integer pkHistorique;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDE", nullable = false)
    private DemandeBO fkDemande;

    @Column(name = "DATE", nullable = false)
    private Timestamp date;

    @Column(name = "STATUT", nullable = false)
    @Size(min = 1, max = 255)
    private String statut;

    @Column(name = "USAGER_ID")
    private Integer usagerId;

    @Column(name = "CONTENU", nullable = false)
    @NotBlank
    @Size(min = 1, max = 10000)
    private String contenu;

    public Integer getPkHistorique() {
        return pkHistorique;
    }

    public void setPkHistorique(Integer pkHistorique) {
        this.pkHistorique = pkHistorique;
    }

    public DemandeBO getFkDemande() {
        return fkDemande;
    }

    public void setFkDemande(DemandeBO fkDemande) {
        this.fkDemande = fkDemande;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Integer getUsagerId() {
        return usagerId;
    }

    public void setUsagerId(Integer usagerId) {
        this.usagerId = usagerId;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
}

package mc.gouv.xaf.back.paiement.data.entity;

import mc.gouv.xaf.back.data.entity.DemandeBO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "PMNT_COMMANDE_DEMANDE")
public class CommandeDemandeBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_COMMANDE_DEMANDE", nullable = false)
    private Integer pkComandeDemande;
    @OneToOne
    @JoinColumn(name = "FK_COMMANDE", nullable = false)
    private CommandeBO commande;

    @OneToOne
    @JoinColumn(name = "FK_DEMANDE", nullable = false)
    private DemandeBO demande;

    private double montant;

    public Integer getPkComandeDemande() {
        return pkComandeDemande;
    }

    public void setPkComandeDemande(Integer pkComandeDemande) {
        this.pkComandeDemande = pkComandeDemande;
    }

    public CommandeBO getCommande() {
        return commande;
    }

    public void setCommande(CommandeBO commande) {
        this.commande = commande;
    }

    public DemandeBO getDemande() {
        return demande;
    }

    public void setDemande(DemandeBO demande) {
        this.demande = demande;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    @Override
    public String toString() {
        return "CommandeDemandeBO{" +
                "pkComandeDemande=" + pkComandeDemande +
                ", commande=" + commande +
                ", demande=" + demande +
                ", montant=" + montant +
                '}';
    }
}

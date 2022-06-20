package mc.gouv.xaf.back.stc.data.entity;

import mc.gouv.xaf.back.data.entity.DemandeBO;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class CommandeDemandeBO {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "commande_id")
    private CommandeBO commande;

    @OneToOne
    @JoinColumn(name = "demande_id")
    private DemandeBO demande;

    private double montant;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
                "id=" + id +
                ", commande=" + commande +
                ", demande=" + demande +
                ", montant=" + montant +
                '}';
    }
}

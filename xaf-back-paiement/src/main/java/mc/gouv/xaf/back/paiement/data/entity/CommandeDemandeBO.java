package mc.gouv.xaf.back.paiement.data.entity;

import mc.gouv.xaf.back.data.entity.DemandeBO;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "PMNT_COMMANDES_DEMANDES")
public class CommandeDemandeBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_COMMANDES_DEMANDES", nullable = false)
    private Integer pkCommandesDemandes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_COMMANDES")
    private CommandeBO commande;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDES")
    private DemandeBO demande;

    private double montant;

    @OneToMany(mappedBy = "commandeDemande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommandeDemandeArticleBO> commandesDemandesArticles;

    public Integer getPkCommandesDemandes() {
        return pkCommandesDemandes;
    }

    public void setPkCommandesDemandes(Integer pkComandeDemande) {
        this.pkCommandesDemandes = pkComandeDemande;
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

    public List<CommandeDemandeArticleBO> getCommandesDemandesArticles() {
        return commandesDemandesArticles;
    }

    public void setCommandesDemandesArticles(List<CommandeDemandeArticleBO> commandesDemandesArticles) {
        this.commandesDemandesArticles = commandesDemandesArticles;
    }

    @Override
    public String toString() {
        return "CommandeDemandeBO{" +
                "pkComandeDemande=" + pkCommandesDemandes +
                ", commande=" + commande +
                ", demande=" + demande +
                ", montant=" + montant +
                '}';
    }
}
